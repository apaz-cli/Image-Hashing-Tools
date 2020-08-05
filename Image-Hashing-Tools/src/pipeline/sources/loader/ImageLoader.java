package pipeline.sources.loader;

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
import pipeline.sources.SavingImageSource;

public class ImageLoader implements SavingImageSource {

	public ImageLoader(File fileOrFolder) throws IllegalArgumentException {
		this(Arrays.asList(fileOrFolder));
	}

	public ImageLoader(List<File> filesOrFolders) throws IllegalArgumentException {
		// Only at most one of the two following functions will do anything, depending
		// on if is file or directory.
		for (File fileOrFolder : filesOrFolders) {
			this.indexFolder(fileOrFolder);
			this.indexFile(fileOrFolder);
		}

		// Removes all files but images from the list.
		this.trimIndex();

		// Now throw an exception if no images are indexed.
		if (files.isEmpty()) {
			throw new IllegalArgumentException(
					"The file or folder specified was valid, but did not contain any images.");
		}
		this.originalFolders = filesOrFolders;
	}

	public ImageLoader(String fileOrFolderPath) throws IllegalArgumentException {
		this(new File(fileOrFolderPath));
	}

	public ImageLoader(String... fileOrFolderPaths) throws IllegalArgumentException {
		this(Arrays.asList(fileOrFolderPaths));
	}

	public ImageLoader(Collection<String> fileOrFolderPaths) {
		this(fileOrFolderPaths.stream().map(s -> new File(s)).collect(Collectors.toList()));
	}

	public ImageLoader(Path fileOrFolderPath) throws IllegalArgumentException {
		this(fileOrFolderPath.toFile());
	}

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

	private void indexFile(File file) {
		if (file.isDirectory() || !file.canRead()) { return; }
		files.add(file);
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
		return this.files.size();
	}

	private List<File> originalFolders;
	private List<File> files = new ArrayList<>(); // Splits into copies on trySplit()
	private List<File> failedLoads = new Vector<>();

	public List<File> getFailedLoads() { return failedLoads; }

	@Override
	public SourcedImage next() {
		// Get file, handle if can't.
		File f;

		if (!files.isEmpty()) f = files.get(files.size() - 1);
		else return null;

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

	private List<String> toDelete = new Vector<>();
	private List<SourcedImage> toSave = new Vector<>();

	@Override
	public synchronized void save() throws IOException {
		synchronized (toDelete) {
			for (String s : this.toDelete) {
				File f = new File(s);
				if (imageContainedHere(f)) f.delete();
			}
			this.toSave.clear();

			for (SourcedImage img : this.toSave) {
				if (img.save() == null) throw new IOException("Failed to save an image to: " + img.getSource());
			}
		}
	}

	// Check for security (Don't delete arbitrary files, that would be bad.)
	private boolean imageContainedHere(File f) {
		Path possibleChildPath = f.toPath().toAbsolutePath();
		for (File parent : this.originalFolders) {
			Path parentPath = parent.toPath().toAbsolutePath();
			if (possibleChildPath.startsWith(parentPath)) return true;
		}
		return false;
	}

	@Override
	public void removeFromSource(String img) {
		toDelete.add(img);
	}

	@Override
	public void removeFromSource(SourcedImage img) {
		if (!img.sourceIsURL())
			throw new IllegalArgumentException("The image must have been generated from this source.");
		toDelete.add(img.getSource());
	}

	@Override
	public void addToSource(SourcedImage img) {
		toSave.add(img);
	}

	@Override
	public int characteristics() {
		return CONCURRENT | NONNULL | IMMUTABLE;
	}

	@Override
	public long estimateSize() {
		return files.size();
	}

	private ImageLoader(List<File> files, List<File> failedLoads) {
		this.files = files;
		this.failedLoads = failedLoads;
	}

	@Override
	public Spliterator<SourcedImage> trySplit() {
		if (files.size() < 50) return null;

		int size = files.size();
		List<File> first = new ArrayList<>(files.subList(0, (size + 1) / 2));
		List<File> second = new ArrayList<>(files.subList((size + 1) / 2, size));

		this.files = first;
		return new ImageLoader(second, failedLoads);
	}

}
