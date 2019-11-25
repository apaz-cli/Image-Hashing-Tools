package pipeline.sources.impl.safebooruscraper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PageRequestTask implements Runnable {

	private PrintWriter outFile;
	private int pageNumber;
	private int reportedOffset;
	private String attribute;
	private AttributeDump taskOwner;

	private static String apiRequestUrl = "https://safebooru.org/index.php?page=dapi&s=post&q=index&limit=100&pid=";

	private int finalCount;

	PageRequestTask(AttributeDump taskOwner, PrintWriter outFile, int pageNumber, String attribute) {
		this.taskOwner = taskOwner;
		this.outFile = outFile;
		this.pageNumber = pageNumber;
		this.attribute = attribute;
	}

	@Override
	public void run() {
		// I'm getting strange HTTP 500 (Server error) responses, so this is my attempt
		// at fixing it by trying again until it works.
		boolean finished = false;
		while (!finished) {
			try {
				// Request next page. Start at 0, requesting the initial one again.
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				URLConnection connection = new URL(apiRequestUrl + pageNumber).openConnection();
				connection.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
				Document document = db.parse(connection.getInputStream());

				// Update the final count, which will tell us if we've reached the end.
				Element root = document.getDocumentElement();
				this.finalCount = Integer.parseInt(root.getAttribute("count"));
				this.reportedOffset = Integer.parseInt(root.getAttribute("offset"));

				// Rip all the urls from the DOM tree
				Element post;
				NodeList posts = root.getChildNodes();
				for (int i = 0; i < posts.getLength(); i++) {
					// This gets the url of the post, which is what we care about, but ignores
					// things like comments/whitespace, and other things we don't care about.
					Node n = posts.item(i);
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						post = (Element) n;
						synchronized (outFile) {
							outFile.println(post.getAttribute(attribute));
							outFile.flush();
						}
					}
				}
				finished = true;

			} catch (IOException e) {

				// If windows is shitting the bed, try again.
				if (e instanceof java.net.UnknownHostException) {
					System.out.println(
							"Please verify that you are connected to the internet. Retrying Task #" + this.pageNumber);
					continue;
				} else if (e instanceof java.net.SocketException) {
					System.out.println("Socket error. Retrying Task #" + this.pageNumber);
					continue;
				}

				// If you get an error in the 500 range, wait 2 seconds and try again.
				if (e.getMessage().matches(".*5.. for URL.*")) {
					System.out.println("Server returned an error. Retrying Task #" + this.pageNumber);
					try {
						Thread.sleep(2000);
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

		// Now that there has been a successful request, and the links have been scraped
		// from it and written to file, report back.
		synchronized (taskOwner) {
			taskOwner.processResult(new PageRequestResult(this.pageNumber, this.finalCount,
					this.pageNumber == 0 ? false : this.reportedOffset == 0));
		}

	}

}
