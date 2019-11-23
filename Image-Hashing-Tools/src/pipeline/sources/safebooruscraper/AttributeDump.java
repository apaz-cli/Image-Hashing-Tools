package pipeline.sources.safebooruscraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class AttributeDump
//implements ImageSource 
{

	public static final String FILE_URL = "file_url";
	public static final String SAMPLE_URL = "sample_url";
	public static final String PREVIEW_URL = "preview_url";
	public static final String ID = "id";
	public static final String SOURCE = "source";

	private static final int trackNumber = 100;
	private static final int poolSize = 16;
	private ExecutorService pool = Executors.newFixedThreadPool(poolSize);

	private static final int postsPerPage = 100;

	private Integer initialCount = 0, finalCount = 0, completedTracks = 0;

	private final String attribute;

	private final PrintWriter outFile;

	private static String apiRequestUrl = "https://safebooru.org/index.php?page=dapi&s=post&q=index&limit=100&pid=";

	public AttributeDump(File linkSaveFile, String attribute)
			throws MalformedURLException, ParserConfigurationException, SAXException, IOException {

		outFile = new PrintWriter(new BufferedWriter(new FileWriter(linkSaveFile)));

		this.attribute = attribute;

		// While we're scraping, it's entirely possible that more images will be added
		// to the site. This will probably screw things up majorly. It will make it so
		// some of the links are duplicated, because they will be pushed back onto the
		// next page. Also, we'll be missing the ones that were added.
		// However, all this validate function does is go grab the new ones. To remove
		// the duplicates, run the resulting outFile through GNU Core Utilities with
		// `sort --unique input-file > output-file`.
		this.scrape();

		// Wait for scraping to be done.
		synchronized (initialCount) {
			try {
				initialCount.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Get the ones that we missed
		this.retrace();
	}

	private void scrape() throws ParserConfigurationException, MalformedURLException, SAXException, IOException {

		this.initialCount = this.getCount();
		this.finalCount = this.initialCount;

		System.out.println("Submitting original tasks.");

		// Fill the pool initially with twice as many tasks as it can execute at once.
		// When they complete, they will automatically add the next ones.
		for (int page = 0; page < AttributeDump.trackNumber; page++) {
			pool.execute(new PageRequestTask(this, this.outFile, page, attribute));
		}

	}

	private void retrace() {
		// Restart the pool for one last bit of work.
		System.out.println("RESTARTING POOL");
		this.pool = Executors.newFixedThreadPool(poolSize);
		this.completedTracks = 0;
		// Redownload all the pages at the beginning with new content on them.
		for (int page = 0; page < (this.finalCount - this.initialCount) / AttributeDump.postsPerPage; page++) {
			pool.execute(new PageRequestTask(this, outFile, page, this.attribute));
		}

		// The pool won't close on its own, so we'll do it manually, which is actually
		// way better.
		try {
			System.out.println("SHUTTING DOWN POOL AGAIN");
			pool.shutdown();
			pool.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void processResult(PageRequestResult result) {
		int nextTaskPage;
		synchronized (this.finalCount) {

			System.out.println("Task " + result.getCurrentPage() + " returned.");

			// Update the final count. Which is to say, if there have been updates on
			// Safebooru, and more images have been added, then the finish line has been
			// moved.
			int newCount = result.getFinalCount();
			if (newCount > this.finalCount) {
				this.finalCount = newCount;
			}

			// Add the next task to the pool, or don't if that page doesn't exist.
			if (!result.isTrackFinished()) {
				nextTaskPage = result.getCurrentPage() + AttributeDump.trackNumber;
				System.out.println("Submitting task " + nextTaskPage);
				pool.execute(new PageRequestTask(this, outFile, nextTaskPage, this.attribute));
			}
			// If the track is completed, increment how many tracks are done. If all tracks
			// are done, shut down
			// the pool.
			else {
				synchronized (this.completedTracks) {
					completedTracks++;
					if (this.completedTracks == AttributeDump.trackNumber) {
						try {
							if (!pool.isShutdown()) {
								System.out.println("SHUTTING DOWN POOL");
								pool.shutdown();
								pool.awaitTermination(60, TimeUnit.SECONDS);
								synchronized (outFile) {
									outFile.close();
								}
								// Tell the constructor to continue, and to redownload the first few pages.
								initialCount.notify();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private int getCount() {
		int count = 0;
		boolean finished = false;
		while (!finished) {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				URLConnection connection = new URL(apiRequestUrl + "0").openConnection();
				connection.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
				Document document = db.parse(connection.getInputStream());
				Element root = document.getDocumentElement();
				count = Integer.parseInt(root.getAttribute("count"));
				finished = true;

			} catch (IOException e) {
				// If windows is shitting the bed, try again.
				if (e instanceof java.net.UnknownHostException) {
					System.out.println("Please verify that you are connected to the internet. Retrying initial count.");
					continue;
				} else if (e instanceof java.net.SocketException) {
					System.out.println("Socket error. Retrying initial count.");
					continue;
				}

				// If you get an error in the 500 range, wait 2 seconds and try again.
				if (e.getMessage().matches(".*5.. for URL.*")) {
					System.out.println("Server returned an error. Retrying initial count.");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						System.err.println(e1.getMessage());
					}
					continue;
				} else {
					// If it isn't one of these things, at least for the purposes of debugging, I
					// want to make it explode.
					e.printStackTrace();
					System.exit(1);
				}
			}
			// These two are unrecoverable.
			catch (SAXException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		// Make a request and get the count from it.

		return count;
	}
}
