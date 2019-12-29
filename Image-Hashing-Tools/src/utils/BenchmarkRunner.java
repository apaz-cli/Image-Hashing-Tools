package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import hash.*;
import hash.implementations.*;
import image.*;
import image.implementations.*;
import pipeline.hasher.HasherOutput;
import pipeline.hasher.ImageHasher;
import pipeline.sources.*;
import pipeline.sources.impl.*;
import pipeline.sources.impl.downloader.URLCollectionDownloader;
import pipeline.sources.impl.loader.ImageLoader;
import pipeline.sources.impl.safebooruscraper.SafebooruScraper;
import pipeline.sources.ops.IImageOperation;
import pipeline.sources.ops.ImageOperation;
import pipeline.sources.ops.ImageOperator;
import pipeline.sources.ops.SourcedImageOperation;
import attack.IAttack;
import attack.implementations.*;

@SuppressWarnings("unused")
public class BenchmarkRunner {

	public static List<URL> images = null;

	static {
		// Lenna, x, serafuku, Astolfo
		String[] urls = new String[] { "https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png",
				"https://images3.alphacoders.com/836/83635.jpg",
				"https://safebooru.org//images/2824/c7f88eef1dda8cf4a5d06c6f732da9e14d08fb38.png",
				"https://pbs.twimg.com/media/D8s6grBU0AAADD3?format=jpg&name=large",
				"https://safebooru.org/images/2855/5b462269fa06bbb9e249698f3153140a110f44be.png" };
		images = new ArrayList<URL>(
				Arrays.asList(urls).stream().map(BenchmarkRunner::urlConstructor).collect(Collectors.toList()));
	}

	private static URL urlConstructor(String url) {
		URL u = null;
		try {
			u = new URL(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return u;
	}

	// ********//
	// * MAIN *//
	// ********//

	public static void main(String[] args) {
		IHashAlgorithm dhash = new DifferenceHash();
		/*
		 * ImageOperator op = new ImageOperator(new SafebooruScraper(), 25,
		 * (SourcedImageOperation) (img) -> { System.out.println(dhash.hash(img));
		 * return img; }); op.invokeAll(); op.close();
		 */

		new ImageHasher(new SafebooruScraper(), dhash, 5, System.out).hashAll();
	}

	// TODO write test for RGBAImage transparency

	// For testing ImageHash serialization
	/*
	 * IHashAlgorithm dhash = new DifferenceHash(); ImageHash h1 = null, h2 = null;
	 * try { h1 = dhash.hash(images.get(0)); } catch (IOException e) {
	 * e.printStackTrace(); }
	 * 
	 * System.out.println(h1.toString());
	 * 
	 * try { File serialized = new File(
	 * "C:\\Users\\PazderaAaron\\Downloads\\Image Hashing Related\\Examples\\dhash\\serialized.hash"
	 * ); h1.writeToNewFile(serialized); h2 = ImageHash.fromFile(serialized); }
	 * catch (FileNotFoundException e) { e.printStackTrace(); } catch (IOException
	 * e) { e.printStackTrace(); }
	 * 
	 * System.out.println("Hashes are equal: " + h1.equals(h2));
	 */

	// For testing pipeline
	/*
	 * URLCollectionDownloader s = null; try { s = new URLCollectionDownloader(new
	 * File("C:\\Users\\PazderaAaron\\Downloads\\Attachment_Links.txt"));
	 * System.out.println("Done Constructing"); } catch (IOException e) {
	 * e.printStackTrace(); }
	 * 
	 * SourcedImageOperation compareDifferentImageDifference = (img) -> {
	 * 
	 * ImageHash h1 = dHash.hash(lastImage); ImageHash h2 = dHash.hash(img); float
	 * diff = h1.percentHammingDifference(h2); // System.out.println(diff + (diff <
	 * .20 ? "\nMATCH:\n" + h1.getSource() + "\n" // + h2.getSource() : ""));
	 * lastImage = img; return img; };
	 * 
	 * ImageOperator operator = new ImageOperator(s,
	 * compareDifferentImageDifference); System.out.println("Executing All");
	 * operator.executeAll(); System.out.println("All Executed");
	 * System.out.println(s.getFailedDownloads()); operator.close();
	 * System.out.println("closed");
	 */

	// For testing ImageHash to/fromString
	/*
	 * IHashAlgorithm dhash = new DifferenceHash();
	 * 
	 * ImageHash h = null; try { h = dhash.hash(images.get(0)); } catch (Exception
	 * e) { e.printStackTrace(); }
	 * 
	 * System.out.println(h);
	 * 
	 * ImageHash j = ImageHash.fromString(h.toString()); System.out.println(j);
	 * System.out.println(h.equals(j));
	 */

}
