package pipeline.sources;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Vector;

import image.implementations.SourcedImage;
import pipeline.ImageSource;
import utils.ImageUtils;

public class URLDownloader implements ImageSource, Closeable {

	File fileOfURLs;
	private List<URL> urls = new ArrayList<>();
	private List<String> failedDownloads = new Vector<>();

	public URLDownloader(File fileOfURLs) throws IOException {
		// Open file
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileOfURLs));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("The file specified could not be found.");
		}

		this.fileOfURLs = fileOfURLs;

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			URL url = null;
			try {
				url = new URL(line);
			} catch (MalformedURLException e) {
				System.err.println("Malformed URL: " + line);
				this.failedDownloads.add(line);
			}
			this.urls.add(url);
		}

		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (this.urls.isEmpty()) { throw new IllegalArgumentException("The collection of urls was empty."); }

	}

	public List<String> getFailedDownloads() { return failedDownloads; }

	@Override
	public SourcedImage next() {

		URL url = null;
		synchronized (this.urls) {
			if (this.urls.isEmpty()) return null;

			url = this.urls.remove(urls.size() - 1);
		}

		BufferedImage f = ImageUtils.openImage(url);
		if (f == null) {
			this.failedDownloads.add(url.toString());
			return this.next();
		}
		return new SourcedImage(f, url);
	}

	@Override
	public int characteristics() { return CONCURRENT | NONNULL | IMMUTABLE; }

	@Override
	public long estimateSize() { return urls.size(); }

	private URLDownloader(List<URL> urls, File fileOfURLs, List<String> failedDownloads) {
		this.urls = urls;
		this.fileOfURLs = fileOfURLs;
		this.failedDownloads = failedDownloads;
	}

	@Override
	public Spliterator<SourcedImage> trySplit() {
		if (this.urls.size() < 50) return null;

		int size = urls.size();
		List<URL> first = new ArrayList<>(urls.subList(0, (size + 1) / 2));
		List<URL> second = new ArrayList<>(urls.subList((size + 1) / 2, size));

		this.urls = first;
		return new URLDownloader(second, fileOfURLs, failedDownloads);
	}

	@Override
	public String getSourceName() { return null; }

	private Set<String> toRemove = Collections.synchronizedSet(new HashSet<>());
	private List<String> toAdd = new Vector<>();

	public void removeFromSource(String source) { this.toRemove.add(source); }

	public void addToSource(SourcedImage img) { this.toAdd.add(img.getSource()); }

	public void addToSource(String source) { this.toAdd.add(source); }

	// Also functions as an access flag for the file for saving.
	private static String lineSep = System.getProperty("line.separator");

	public void save() throws IOException {
		synchronized (lineSep) {
			File tempFile = new File("TEMP_URLSOURCE_SAVE_FILE.txt");

			BufferedReader reader = new BufferedReader(new FileReader(this.fileOfURLs));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			// Remove toRemove
			String currentURL;
			while ((currentURL = reader.readLine()) != null) {

				// Read
				String trimmedLine = currentURL.trim();

				// Ignore to remove
				if (toRemove.contains(trimmedLine)) continue;

				// Write if not removed
				writer.write(currentURL + lineSep);
			}
			toRemove.clear();

			// Add toAdd
			for (String addURL : toAdd) {
				writer.write(addURL + lineSep);
			}
			toAdd.clear();

			writer.close();
			reader.close();

			tempFile.renameTo(this.fileOfURLs);
		}
	}

	@Override
	public void close() throws IOException { save(); }

}
