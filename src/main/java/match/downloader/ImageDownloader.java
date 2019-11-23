package match.downloader;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageDownloader {

	private static ExecutorService pool = Executors.newWorkStealingPool(20);

	private volatile Stack<BufferedImage> imageBuffer = new Stack<>();

	// Notified to show that buffer is no longer empty
	private Integer bufferSize;

	// Closes itself when finished
	private BufferedReader reader;
	private boolean finished = false;

	public ImageDownloader(File urls, int bufferSize)
			throws MalformedURLException, IOException, InterruptedException, ExecutionException {
		this.reader = new BufferedReader(new FileReader(urls));
		this.bufferSize = bufferSize;
		this.beginDownloading();
	}

	private void beginDownloading() throws IOException, MalformedURLException, InterruptedException, ExecutionException {

		// Add the appropriate number of tasks to the pool to fill the buffer.
		String line;
		for (int i = 0; (((line = reader.readLine()) != null) && i < bufferSize); i++) {
			pool.execute(new DownloadTask(this, line));
		}

		// When nextImage() is called, tasks will be added.
	}

	void receiveImage(BufferedImage image) {
		// Buffer should always be able to recieve. If somehow it overfills that's fine.
		imageBuffer.add(image);
		// Wake a thread waiting on getNextImage()
		if (!this.finished) {
			synchronized (bufferSize) {
				bufferSize.notify();
			}
		}
	}

	// Returns null when finished and empty.
	public BufferedImage getNextImage() throws IOException {

		// Add another task to the pool, or mark finished if the file is completed.
		if (!finished) {
			String line;
			line = reader.readLine();
			if (line != null) {
				pool.execute(new DownloadTask(this, line));
			} else {
				this.finished = true;
				this.reader.close();
			}
		}

		// If buffer is empty, then wait for an image.
		if (imageBuffer.empty()) {
			try {
				synchronized (bufferSize) {
					bufferSize.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// If null, try again.
		BufferedImage popped = imageBuffer.pop();
		return popped != null ? popped : this.getNextImage();
	}

}
