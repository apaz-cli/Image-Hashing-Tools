package pipeline.sources.downloader;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import image.implementations.SourcedImage;
import pipeline.ImageSource;
import utils.ImageUtils;

public class URLCollectionDownloader implements ImageSource {

	private ExecutorService loadThread = Executors.newWorkStealingPool(25);

	private List<URL> urls = new ArrayList<>();
	private SynchronousQueue<SourcedImage> imageBuffer = new SynchronousQueue<>();

	private CountDownLatch latch;

	private List<String> failedDownloads = new ArrayList<>();

	/**
	 * Not suitable for files larger than can fit in memory. For that use
	 * URLFileDownloader.
	 * 
	 * @param fileOfURLs
	 */
	public URLCollectionDownloader(File fileOfURLs) throws IOException {
		// Open file
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileOfURLs));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("The file specified could not be found.");
		}

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			URL url = null;
			try {
				url = new URL(line);
			} catch (MalformedURLException e) {
				System.err.println("Lost Malformed URL: " + line);
				this.failedDownloads.add(line);
			}
			this.urls.add(url);
		}

		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (this.urls.isEmpty()) {
			throw new IllegalArgumentException("The collection of urls was empty.");
		}

		this.latch = new CountDownLatch(this.urls.size());

		for (int i = 0; i < 25; i++) {
			loadThread.execute(new DownloadTask(this));
		}
	}

	public URLCollectionDownloader(Collection<?> urls) {
		if (urls.isEmpty()) {
			throw new IllegalArgumentException("The collection of urls was empty.");
		}

		urls.stream().forEach((url) -> {
			if (url instanceof URL) {
				this.urls.add((URL) url);
			} else if (url instanceof String) {
				URL constructed = null;
				String sURL = (String) url;
				try {
					constructed = new URL(sURL);
				} catch (MalformedURLException e) {
					System.err.println("Lost Malformed URL: " + sURL);
					this.failedDownloads.add(sURL);
				}
				this.urls.add(constructed);
			} else {
				System.err.println("Could not cast from the type of this collection to URL.");
			}
		});

		// Now throw an exception if no images are indexed.
		if (this.urls.isEmpty()) {
			throw new IllegalArgumentException("No URLs could be constructed from the collection.");
		}

		this.latch = new CountDownLatch(this.urls.size());

		for (int i = 0; i < 6; i++) {
			loadThread.execute(new DownloadTask(this));
		}
	}

	public URLCollectionDownloader(String url) throws MalformedURLException {
		this(new URL(url));
	}

	public URLCollectionDownloader(URL url) throws MalformedURLException {
		this(Arrays.asList(new URL[] { url }));
	}

	void downloadImage() {

		URL url = null;
		synchronized (this) {
			synchronized (latch) {
				synchronized (this.urls) {
					if (!this.urls.isEmpty()) {
						url = this.urls.get(0);
						this.latch.countDown();
					} else {
						return;
					}
				}
			}
		}

		BufferedImage f = null;
		try {
			f = ImageUtils.openImage(url);
		} catch (IOException e) {

		}

		if (f == null) {
			return;
		}

		synchronized (this) {
			synchronized (latch) {
				try {
					this.imageBuffer.put(new SourcedImage(f, url));
				} catch (InterruptedException e) {
				}
				latch.notify();
			}
		}

		return;
	}

	public List<String> getFailedDownloads() {
		return failedDownloads;
	}

	@Override
	public SourcedImage nextImage() {
		return null;
	}

	void shutdownPool() {

	}

	@Override
	public void close() {

	}

}
