package pipeline.sources.impl.downloader;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;
import pipeline.sources.TerminalImage;
import utils.ImageUtils;

public class URLCollectionDownloader implements ImageSource {

	private static SourcedImage TERMINALIMAGE = new TerminalImage(null);

	private ExecutorService loadThread = Executors.newWorkStealingPool(25);

	private List<URL> urls = new ArrayList<>();
	private SynchronousQueue<SourcedImage> imageBuffer = new SynchronousQueue<>();
	private Object bufferAccessFlag = new Object();

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

		for (int i = 0; i < 6; i++) {
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
		// Get file, handle if can't.
		URL u;
		synchronized (urls) {
			if (!urls.isEmpty()) {
				u = urls.remove(0);
			} else {
				// If it's null, we're done. We should inform the buffer.
				try {
					// Since we've synchronized, this should be the last one in.
					synchronized (this) {
						new DownloaderShutdownThread(this).start();
					}
					synchronized (bufferAccessFlag) {
						System.out.println("Adding to buffer: " + TERMINALIMAGE);
						imageBuffer.put(TERMINALIMAGE);
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
			img = ImageUtils.openImage(u);
			if (img == null) {
				synchronized (failedDownloads) {
					// Now that we've failed gracefully, we can try again.
					this.failedDownloads.add(u.toString());
					this.downloadImage();
					return;
				}
			}
		} catch (Exception e) {
			// Now that we've failed gracefully, we can try again.
			// TODO replace image error thingy
			System.err.println(
					"Lost image to Error: " + "Link" + " Reason: " + e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			synchronized (failedDownloads) {
				this.failedDownloads.add(u.toString());
				this.downloadImage();
				return;
			}
		}

		// Now that we have a non-null image we can add it to the buffer and inform one
		// waiting nextImage() method that it's ready.
		try {
			synchronized (bufferAccessFlag) {
				if (imageBuffer != null) {
					System.out.println("Adding to buffer: " + "Link");
					imageBuffer.put(new SourcedImage(img, u));
				}
			}
		} catch (InterruptedException e) {
		}

	}

	public List<String> getFailedDownloads() {
		return failedDownloads;
	}

	@Override
	public SourcedImage nextImage() {
		synchronized (this) {
			if (this.urls == null) {
				return null;
			}
			try {
				loadThread.execute(new DownloadTask(this));
			} catch (RejectedExecutionException e) {
				return null;
			}

		}

		synchronized (this) {
			SourcedImage img = null;
			try {
				if (this.imageBuffer == null) {
					return null;
				}
				img = imageBuffer.take();
			} catch (InterruptedException e) {
			}
			return img != TERMINALIMAGE ? img : null;
		}
	}

	void shutdownPool() {
		synchronized (this) {
			synchronized (loadThread) {
				if (!loadThread.isShutdown()) {
					try {
						loadThread.shutdown();
						loadThread.awaitTermination(1, TimeUnit.MINUTES);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	@Override
	public void close() {
		synchronized (this) {
			synchronized (this) {
				this.shutdownPool();
			}
			synchronized (urls) {
				this.urls = null;
			}
			synchronized (bufferAccessFlag) {
				this.imageBuffer = null;
			}
			synchronized (failedDownloads) {
				this.failedDownloads = null;
			}
		}
	}

}
