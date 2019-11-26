package pipeline.sources.impl.loader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import javax.imageio.ImageIO;

import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;

public class ImageLoader implements ImageSource {

	private static SourcedImage TERMINALIMAGE = new TerminalImage(null);

	private ExecutorService loadThread = Executors.newWorkStealingPool(3);

	private List<File> files = new LinkedList<>();
	private SynchronousQueue<SourcedImage> imageBuffer = new SynchronousQueue<>();
	private Object bufferAccessFlag = new Object();

	private List<File> failedLoads = new ArrayList<>();

	public ImageLoader(File fileOrFolder) throws IllegalArgumentException {
		// Only at most one of the two following functions will do anything, depending
		// on if is file or directory.
		this.indexFolder(fileOrFolder);
		this.indexFile(fileOrFolder);

		// If no files are indexed, trimFolder() will throw an exception.
		this.trimIndex();

		// Now throw an exception if no images are indexed.
		if (files.isEmpty()) {
			throw new IllegalArgumentException(
					"The file or folder specified was valid, but did not contain any images.");
		}
		for (int i = 0; i < 6; i++) {
			loadThread.execute(new LoadTask(this));
		}
	}

	public ImageLoader(String fileOrFolderPath) throws IllegalArgumentException {
		this(new File(fileOrFolderPath));
	}

	public ImageLoader(Path fileOrFolderPath) throws IllegalArgumentException {
		this(fileOrFolderPath.toFile());
	}

	private void indexFolder(File folder) {
		if (!folder.isDirectory() || !folder.canRead()) {
			return;
		}

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
		if (file.isDirectory() || !file.canRead()) {
			return;
		}
		files.add(file);
	}

	private void trimIndex() {
		// This function doesn't have to be synchronized, because it is only called in
		// the constructor.
		if (this.files.isEmpty()) {
			throw new IllegalArgumentException("Couldn't load any files out of the file specified.");
		}

		for (int i = 0; i < this.files.size(); i++) {
			File f = this.files.get(i);
			Path p = f.toPath();
			try {
				String mimeType = Files.probeContentType(p);
				if (mimeType == null) {
					files.remove(f);
				} else if (!mimeType.contains("image/")) {
					files.remove(f);
				}
			} catch (IOException e) {
				e.printStackTrace();
				files.remove(f);
			}
		}
	}

	void loadImage() {

		// Get file, handle if can't.
		File f;
		synchronized (files) {
			if (!files.isEmpty()) {
				f = files.remove(0);
			} else {
				// If it's null, we're done. We should inform the buffer.
				try {
					// Since we've synchronized, this should be the last one in.
					imageBuffer.put(TERMINALIMAGE);
					synchronized (this) {
						this.loadThread.shutdownNow();
					}
				} catch (InterruptedException e) {
				}
				return;
			}
		}

		// Beyond this point, we have a valid file.
		// Get image by loading that file, handle if can't.
		BufferedImage img = null;
		try {
			img = ImageIO.read(f);
			if (img == null) {
				synchronized (failedLoads) {
					// Now that we've failed gracefully, we can try again.
					this.failedLoads.add(f);
					this.loadImage();
					return;
				}
			}
		} catch (Exception e) {
			// Now that we've failed gracefully, we can try again.
			System.err.println(
					"Lost image to Error: " + f + " Reason: " + e.getClass().getName() + ": " + e.getMessage());
			synchronized (failedLoads) {
				this.failedLoads.add(f);
				this.loadImage();
				return;
			}
		}

		// Now that we have a non-null image we can add it to the buffer and inform one
		// waiting nextImage() method that it's ready.
		try {
			synchronized (bufferAccessFlag) {
				if (imageBuffer != null) {
					imageBuffer.put(new SourcedImage(img, f));
				}
			}
		} catch (InterruptedException e) {
		}

	}

	public List<File> getFailedLoads() {
		return failedLoads;
	}
	
	@Override
	public SourcedImage nextImage() {
		synchronized (this) {
			if (loadThread != null) {
				loadThread.execute(new LoadTask(this));
			}
		}
		SourcedImage img = null;
		try {
			synchronized (this) {
				if (this.imageBuffer == null) {
					return null;
				}
				img = imageBuffer.take();
			}
		} catch (InterruptedException e) {
		}
		return img != TERMINALIMAGE ? img : null;
	}

	@Override
	public void close() {
		synchronized (this) {
			loadThread.shutdownNow();
			synchronized (files) {
				this.files = null;
			}
			synchronized (bufferAccessFlag) {
				this.imageBuffer = null;
			}
			synchronized (failedLoads) {
				this.failedLoads = null;
			}
		}
	}

}
