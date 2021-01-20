package pipeline.sources;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import image.implementations.SourcedImage;
import pipeline.ImageSource;
import utils.ImageUtils;

public class ImageLoader implements ImageSource {

	public ImageLoader(String fileOrFolderPath) throws IllegalArgumentException {
		this(new File(fileOrFolderPath));
	}

	public ImageLoader(Path fileOrFolderPath) throws IllegalArgumentException {
		this(fileOrFolderPath.toFile());
	}

	public ImageLoader(File folder) throws IllegalArgumentException {
		if (folder == null) throw new NullPointerException("The folder cannot be null.");
		if (!folder.isDirectory()) throw new IllegalArgumentException("The specified folder is not a folder.");
		if (!folder.canRead()) throw new IllegalArgumentException("The specified folder cannot be read from.");
		this.originalFolder = folder;

		// Walk the folder path and add all the files found.
		this.files = new ArrayList<>(indexFolder(folder));

		// Remove all files but images from the list.
		this.trimIndex();
	}

	private List<File> indexFolder(File folder) {
		final List<File> files = new Vector<>();

		try {
			final ExecutorService threadpool = Executors.newWorkStealingPool();
			
			class IndexTask implements Runnable {
				File target;
				IndexTask(File target) {
					this.target = target;
				}
				@Override
				public void run() {
					File[] filesAndFolders = target.listFiles();
					for (File f : filesAndFolders) {
						if (f.isDirectory()) {
							threadpool.execute(new IndexTask(f));
						} else {
							files.add(f);
						}
					}
				}
			}
			
			threadpool.execute(new IndexTask(folder));
			threadpool.shutdown();
			threadpool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return files;
	}

	private void trimIndex() {
		// Removes all files but images from the list.
		// Also remove svg and gif images if
		List<File> toRemove = this.files.parallelStream().filter(f -> {
			// return true for files we want to remove
			try {
				String mimeType = Files.probeContentType(f.toPath());

				if (mimeType == null) return true;
				if (!mimeType.contains("image/")) return true;
				if (!ImageUtils.READSVG && mimeType.contains("svg+xml")) return true;
				if (!ImageUtils.READGIF && mimeType.contains("image/gif")) return true;
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return true;
			}
		}).collect(Collectors.toList());

		this.files.removeAll(toRemove);
	}

	public int estimatedRemaining() {
		synchronized (files) {
			return this.files.size();
		}
	}

	private File originalFolder;
	private List<File> files = new ArrayList<>(); // Splits into copies on trySplit()
	private List<File> failedLoads = new Vector<>();

	public List<File> getRemainingItems() { return files; }

	public List<File> getFailedLoads() { return failedLoads; }

	@Override
	public SourcedImage next() {
		// Get file, handle if can't.
		File f;
		synchronized (files) {
			if (!files.isEmpty()) f = files.remove(files.size() - 1);
			else return null;
		}

		// Beyond this point, we have a file.
		// Get image by loading that file, handle if it's invalid.
		BufferedImage img = null;
		try {
			img = ImageUtils.openImage(f);
			if (img == null) {
				this.failedLoads.add(f);
				return this.next();
			}
		} catch (Exception e) {

			// Capture the exception and analyze it
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String trace = sw.toString();

			// Ignore the ImageIO .gif codec bug
			if (!trace.contains("at java.desktop/javax.imageio.ImageIO.read(ImageIO.java:")) {
				System.err.println("Failed to load image. Error: " + f + " Reason: " + e.getClass().getName() + ": "
						+ e.getMessage());
			}

			// Now that we've failed gracefully, we can try again.
			this.failedLoads.add(f);
			return this.next();
		}

		return new SourcedImage(img, f);
	}

	// Check for security (Don't delete arbitrary files, that would be bad.)
	public boolean imageContainedHere(File f) {
		Path possibleChildPath = f.toPath().toAbsolutePath();
		Path parentPath = this.originalFolder.toPath().toAbsolutePath();

		return possibleChildPath.startsWith(parentPath);
	}

	@Override
	public int characteristics() {
		return CONCURRENT | NONNULL | IMMUTABLE;
	}

	@Override
	public long estimateSize() {
		synchronized (files) {
			return files.size();
		}
	}

	@Override
	public String getSourceName() { return this.originalFolder.toString(); }

	public File getOriginalFolder() { return this.originalFolder; }

	private ImageLoader(List<File> files, List<File> failedLoads) {
		this.files = files;
		this.failedLoads = failedLoads;
	}

	@Override
	public Spliterator<SourcedImage> trySplit() {
		synchronized (files) {
			if (files.size() < 50) return null;

			int size = files.size();
			List<File> first = new ArrayList<>(files.subList(0, (size + 1) / 2));
			List<File> second = new ArrayList<>(files.subList((size + 1) / 2, size));

			this.files = first;
			return new ImageLoader(second, failedLoads);
		}
	}

}
