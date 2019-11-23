package pipeline.sources.impl.downloader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import pipeline.sources.SourcedImage;
import utils.ImageUtils;

class DownloadTask implements Runnable {

	private BufferedImage image = null;
	private final String imgURL;
	private final ImageDownloader taskOwner;

	public DownloadTask(ImageDownloader taskOwner, String url) {
		this.taskOwner = taskOwner;
		this.imgURL = url;
	}

	@Override
	public void run() {
		try {
			// If this fails during openImage somehow, image will be null. This is good.
			image = ImageUtils.openImage(new URL(imgURL));
		} catch (IOException | NullPointerException e) {
			if (e instanceof NullPointerException) {
				System.err.println("Null image URL somehow passed to a DownloadTask.");
				return;
			}
			// Don't log what URL failed to download, because we'll have the downloader do
			// it.
		}

		// Return a sourcedImage with the results. A null image means download failed.
		// It's better to do it this way, because then the downloader knows which URLs
		// failed.
		try {
			this.taskOwner.receiveImage(new SourcedImage(image, imgURL));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}