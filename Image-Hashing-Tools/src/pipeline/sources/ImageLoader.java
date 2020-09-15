package pipeline.sources;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import image.implementations.SourcedImage;
import pipeline.ImageSource;

public class ImageLoader implements ImageSource {

	public ImageLoader(File folder) throws IllegalArgumentException {

		this.indexFolder(folder);

		// Removes all files but images from the list.
		this.trimIndex();

		// Now throw an exception if no images are indexed.
		if (files.isEmpty()) {
			throw new IllegalArgumentException(
					"The file or folder specified was valid, but did not contain any images.");
		}
		this.originalFolder = folder;
	}

	public ImageLoader(List<File> filesOrFolders) throws IllegalArgumentException {

	}

	public ImageLoader(String fileOrFolderPath) throws IllegalArgumentException { this(new File(fileOrFolderPath)); }

	public ImageLoader(String... fileOrFolderPaths) throws IllegalArgumentException {
		this(Arrays.asList(fileOrFolderPaths));
	}

	public ImageLoader(Collection<String> fileOrFolderPaths) {
		this(fileOrFolderPaths.stream().map(s -> new File(s)).collect(Collectors.toList()));
	}

	public ImageLoader(Path fileOrFolderPath) throws IllegalArgumentException { this(fileOrFolderPath.toFile()); }

	private void indexFolder(File folder) {
		if (!folder.isDirectory() || !folder.canRead()) { return; }

		File[] filesAndFolders = folder.listFiles();
		for (File f : filesAndFolders) {
			if (f.isDirectory()) {
				this.indexFolder(f);
			} else {
				files.add(f);
			}
		}
	}

	private void trimIndex() {
		// Removes all files but images from the list.
		files.removeAll(files.parallelStream().filter(f -> {
			try {
				String mimeType = Files.probeContentType(f.toPath());
				return (mimeType == null) || (!mimeType.contains("image/"));
			} catch (IOException e) {
				e.printStackTrace();
				return true;
			}
		}).collect(Collectors.toList()));
	}

	public int estimatedRemaining() {
		synchronized (files) {
			return this.files.size();
		}
	}

	private File originalFolder;
	private List<File> files = new ArrayList<>(); // Splits into copies on trySplit()
	private List<File> failedLoads = new Vector<>();

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
			img = ImageIO.read(f);
			if (img == null) {
				this.failedLoads.add(f);
				return this.next();
			}
		} catch (Exception e) {

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
		if (possibleChildPath.startsWith(parentPath)) return true;

		return false;
	}

	@Override
	public int characteristics() { return CONCURRENT | NONNULL | IMMUTABLE; }

	@Override
	public long estimateSize() {
		synchronized (files) {
			return files.size();
		}
	}

	@Override
	public String getSourceName() { return this.originalFolder.toString(); }

	public File getFolder() { return this.originalFolder; }

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
