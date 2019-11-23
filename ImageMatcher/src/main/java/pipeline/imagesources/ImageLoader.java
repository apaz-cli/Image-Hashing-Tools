package pipeline.imagesources;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageLoader implements ImageSource {

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
				if (!mimeType.contains("image")) {
					files.remove(f);
				}
			} catch (IOException e) {
				e.printStackTrace();
				files.remove(f);
			}
		}
		this.imageNumber = files.size();
	}

	private void loadImages() {
		synchronized (files) {
			for (int i = 0; i < files.size() && i < 50; i++) {
				File f = files.remove(0);
				BufferedImage img = null;
				try {
					img = ImageIO.read(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				synchronized (imageBuffer) {
					imageBuffer.add(new SourcedImage(img, f));
					imageBuffer.notify();
				}
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

			synchronized (imagesIterated) {
				imagesIterated++;
				if (this.imagesIterated != this.imageNumber) {
					if (this.imageBuffer.size() < 25) {
						this.loadImages();
					}
				}
			}
		}

		return img;
	}

	@Override
	public void close() {
		synchronized (imageBuffer) {
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
