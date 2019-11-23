package match.downloader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

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
			image = ImageUtils.openImage(new URL(imgURL));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		// Returns null if exception was thrown
		this.taskOwner.receiveImage(image);
	}

}