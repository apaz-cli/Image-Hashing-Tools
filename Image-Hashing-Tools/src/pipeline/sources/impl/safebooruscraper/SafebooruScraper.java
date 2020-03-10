package pipeline.sources.impl.safebooruscraper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import image.implementations.SourcedImage;
import pipeline.sources.ImageSource;
import utils.ImageUtils;

public class SafebooruScraper implements ImageSource {

	public static final String FILE_URL = "file_url";
	public static final String SAMPLE_URL = "sample_url";
	public static final String PREVIEW_URL = "preview_url";
	public static final String ID = "id";
	public static final String SOURCE = "source";

	private static String apiRequestURL = "https://safebooru.org/index.php?page=dapi&s=post&q=index&limit=100&pid=";
	private String attribute = null;

	private List<String> imageURLs = new Vector<>();
	private int currentPage = 0;

	/** uses "file_url" */
	public SafebooruScraper() {
		this.attribute = "file_url";
	}

	public SafebooruScraper(String attribute) {
		this.attribute = attribute;
	}

	@Override
	public SourcedImage nextImage() {
		String surl = null;
		synchronized (imageURLs) {
			if (this.imageURLs.isEmpty()) {
				this.requestPage(currentPage);
				currentPage++;
			}

			// If nothing was added (And there weren't any errors that made it explode) then
			// we must have reached the end.
			if (this.imageURLs.isEmpty()) {
				return null;
			}

			// Otherwise, we can continue with the last url of the list.
			surl = imageURLs.remove(imageURLs.size() - 1);
		}

		URL imgURL = null;
		try {
			imgURL = new URL(surl);
		} catch (MalformedURLException e) {
			System.err.println("Safebooru returned an attribute that is not a well-formed URL: " + surl);
		}

		SourcedImage img = null;
		try {
			// If for some reason the download fails, this will return null.
			img = ImageUtils.openImageSourced(imgURL);
		} catch (IOException e) {
			System.err.println("Image could not be opened.");
			e.printStackTrace();
		}

		// If the download fails, try again from the beginning with a new link.
		return img == null ? this.nextImage() : img;
	}

	private void requestPage(int offset) {
		try {
			// Request next page. Start at 0, requesting the initial one again.
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			URLConnection connection = new URL(apiRequestURL + offset).openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			Document document = db.parse(connection.getInputStream());

			// Update the final count, which will tell us if we've reached the end. That
			// we've added nothing is the signal to let nextImage() know that there are no
			// further images.
			Element root = document.getDocumentElement();
			int count = Integer.parseInt(root.getAttribute("count"));
			if (count == 0) {
				return;
			}

			// Rip all the urls from the DOM tree, and add them to the list.
			Element post;
			NodeList posts = root.getChildNodes();
			for (int i = 0; i < posts.getLength(); i++) {
				// This gets the url of the post, which is what we care about, but ignores
				// things like comments/whitespace, and other things we don't care about.
				Node n = posts.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					post = (Element) n;
					String imageURL = post.getAttribute(attribute);
					if (!this.imageURLs.contains(imageURL)) {
						this.imageURLs.add(imageURL);
					}
				}
			}

			// The logic of the method is finished, handling errors gets a little bit
			// unruly.
		} catch (IOException e) {
			// If windows is screwing up, try again. I got these when I closed my laptop.
			if (e instanceof java.net.UnknownHostException) {
				try {
					Thread.sleep(5000L);
				} catch (InterruptedException e1) {
				}
				this.requestPage(offset);
			} else if (e instanceof java.net.SocketException) {
				// This is indicative of a connection issue unrelated to not having Internet.
				this.requestPage(offset);
			}

			// If you get an error in the 500 range, wait 2 seconds and try again.
			if (e.getMessage().matches(".*5.. for URL.*")) {
				try {
					Thread.sleep(2000);
					this.requestPage(offset);
				} catch (InterruptedException e2) {
					System.err.println(e2.getMessage());
				}

			} else {
				// If it isn't one of these things, at least for the purposes of debugging, I
				// want to make it explode.

				// In this way, this method is guaranteed to somehow work, overflow the
				// function stack, or otherwise halt the entire program.
				System.err.println("An unknown error has occurred. "
						+ "Please create an issue at https://github.com/Aaron-Pazdera/Open-Image-Hashing-Tools "
						+ "with the following stack trace:");
				e.printStackTrace();
				System.exit(1);
			}
		}
		// These two I assume to be unrecoverable.
		catch (ParserConfigurationException e) {
			System.err.println("Unrecoverable error configuring parser.");
			e.printStackTrace();
			System.exit(2);
		} catch (SAXException e) {
			System.err.println("Unrecoverable error parsing SAX DOM tree.");
			e.printStackTrace();
			System.exit(2);
		}
	}

	@Override
	public void close() {
	}

}
