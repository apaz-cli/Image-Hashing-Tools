package pipeline.sources.impl.loader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;

public class ImageLoader implements ImageSource {

	private ExecutorService loadThread = Executors.newSingleThreadExecutor();

	private List<File> files = new LinkedList<>();
	private List<SourcedImage> imageBuffer = new LinkedList<>();

	private int imageNumber = 0;
	private Integer imagesIterated = 0;

	public ImageLoader(File fileOrFolder) {
		// Only at most one of the two following functions will do anything, depending
		// on if is file or directory.
		this.indexFolder(fileOrFolder);
		this.indexFile(fileOrFolder);

		this.trimFolder();
		this.loadImages();
	}

	public ImageLoader(String fileOrFolderPath) {
		this(new File(fileOrFolderPath));
	}
	
	public ImageLoader(Path fileOrFolderPath) {
		this(fileOrFolderPath.toFile());
	}

	private void indexFile(File file) {
		if (file.isDirectory() || !file.canRead()) {
			return;
		}
		files.add(file);
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

	private void trimFolder() {
		for (int i = 0; i < this.files.size(); i++) {
			File f = this.files.get(i);
			Path p = f.toPath();
			try {
				String mimeType = Files.probeContentType(p);
				if (mimeType == null) {
					files.remove(f);
				} else if (!mimeType.contains("image")) {
					files.remove(f);
				}
			} catch (IOException e) {
				e.printStackTrace();
				files.remove(f);
			}
		}
		this.imageNumber = files.size();
	}

	void loadImages() {
		// At most 50 times
		for (int i = 0; i < 50; i++) {
			File f;
			synchronized (files) {
				f = !files.isEmpty() ? files.remove(0) : null;
			}
			if (f == null) {
				return;
			}

			BufferedImage img = null;
			try {
				img = ImageIO.read(f);
			} catch (Exception e) {
				System.err.println("Lost image: " + f + ". Error: Type: " + e.getClass().getName() + " Message: "
						+ e.getMessage());
			}

			if (img == null) {
				i--;
				continue;
			}

			synchronized (imageBuffer) {
				imageBuffer.add(new SourcedImage(img, f));
				imageBuffer.notify();
			}
		}
	}

	@Override
	public SourcedImage nextImage() {
		if (this.imagesIterated >= this.imageNumber) {
			return null;
		}

		SourcedImage img = null;
		synchronized (imageBuffer) {
			if (imageBuffer.isEmpty()) {
				try {
					imageBuffer.wait();
				} catch (InterruptedException e) {
				}
			}

			img = imageBuffer.remove(0);
		}

		boolean load = false;
		synchronized (imagesIterated) {
			imagesIterated++;
			if (this.imagesIterated != this.imageNumber && this.imageBuffer.isEmpty()) {
				loadThread.execute(new LoadTask(this));
			}
		}

		if (load) {
			this.loadImages();
		}

		return img;
	}

	@Override
	public void close() {
		synchronized (imageBuffer) {
			loadThread.shutdownNow();
			imageBuffer = new LinkedList<>();
			synchronized (files) {
				files = new LinkedList<>();
			}
			synchronized (imagesIterated) {
				this.imageNumber = 0;
				this.imagesIterated = 0;
			}

		}
	}

}
