package pipeline.imagesources;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ImageDownloader implements ImageSource {

	private static ExecutorService pool = Executors.newWorkStealingPool(20);

	private BlockingQueue<SourcedImage> imageBuffer = new LinkedBlockingQueue<>();

	// Notified to show that buffer is no longer empty
	private Integer bufferSize;

	// Closes itself when finished
	private BufferedReader reader;
	private Boolean finished = false;

	private List<String> failedDownloads = new LinkedList<>();

	public ImageDownloader(File urls, int bufferSize)
			throws MalformedURLException, IOException, InterruptedException, ExecutionException {
		this.reader = new BufferedReader(new FileReader(urls));
		this.bufferSize = bufferSize;
		this.beginDownloading();
	}

	public ImageDownloader(File urls)
			throws MalformedURLException, IOException, InterruptedException, ExecutionException {
		this(urls, 40);
	}

	private void beginDownloading()
			throws IOException, MalformedURLException, InterruptedException, ExecutionException {
		// Add the appropriate number of tasks to the pool to initially fill the buffer.
		// Will just ignore extra submissions if we submit more than there is work to
		// do.
		for (int i = 0; i < bufferSize; i++) {
			this.submitTask();
		}
		// When nextImage() is called, new tasks will be added.
	}

	void receiveImage(SourcedImage image) throws InterruptedException {
		if (image.unwrap() != null) {
			imageBuffer.put(image);
		} else {
			try {
				this.submitTask();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Adds a task to the pool if not finished. If finished, marks finished.
	// Returns true if task is actually added, false if finished.
	private boolean submitTask() throws IOException {
		synchronized (finished) {
			synchronized (reader) {
				if (!finished) {
					String line;
					line = reader.readLine();
					if (line != null) {
						pool.execute(new DownloadTask(this, line));
					} else {
						this.finished = true;
						this.reader.close();
					}
					return true;
				}
			}
		}
		return false;
	}

	public BufferedImage nextBufferedImage() {
		SourcedImage next = this.nextImage();
		// Check handles end of stream.
		return next == null ? null : next.unwrap();
	}

	@Override
	public SourcedImage nextImage() {
		// Submit a new task to the pool. Then we replenish the tasks as quickly as we
		// complete them, and there will always be a task to execute.
		try {
			this.submitTask();
		} catch (IOException e) {
			// If somehow there's a horrific error reading the file return null, signaling
			// end of stream.
			e.printStackTrace();
			return null;
		}

		// Wait for the next image to be put into the queue.
		SourcedImage popped = null;
		try {
			popped = imageBuffer.poll(2, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// If none appear for a really long time, signal end of stream.
		if (popped == null) {
			return null;
		}

		// Handle task failure by logging the failed download and trying to return
		// another.
		// This cannot create infinite recursion, because there's a finite number of
		// images to download.
		if (popped.unwrap() == null) {
			this.failedDownloads.add(popped.getSource());
			return this.nextImage();
		}

		// If it's valid, return it.
		return popped;

	}

	public List<String> getFailedDownloads() {
		return this.failedDownloads;
	}

	@Override
	public void close() {
		// TODO finish implementing
		try {
			pool.shutdownNow();
			pool = Executors.newWorkStealingPool(20);
		} catch (Exception e) {
		}
		
		
	}

}
