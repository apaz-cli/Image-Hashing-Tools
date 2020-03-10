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
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

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
import pipeline.sources.operator.IImageOperation;
import pipeline.sources.operator.ImageOperator;
import attack.*;
import attack.convolutions.*;
import attack.other.*;

@SuppressWarnings("unused")
public class BenchmarkRunner {

	public static List<URL> IMAGES = null;

	static {
		// Lenna, x, serafuku, Astolfo
		String[] urls = new String[] { "https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png",
				"https://images3.alphacoders.com/836/83635.jpg",
				"https://safebooru.org//IMAGES/2824/c7f88eef1dda8cf4a5d06c6f732da9e14d08fb38.png",
				"https://pbs.twimg.com/media/D8s6grBU0AAADD3?format=jpg&name=large",
				"https://safebooru.org//IMAGES/2855/5b462269fa06bbb9e249698f3153140a110f44be.png" };
		IMAGES = new ArrayList<URL>(
				Arrays.asList(urls).stream().map(BenchmarkRunner::urlConstructor).collect(Collectors.toList()));
	}

	private static URL urlConstructor(String url) {
		URL u = null;
		try {
			u = new URL(url);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return u;
	}

	// ********//
	// * MAIN *//
	// ********//

	public static void main(String[] args) {
		/*
		 * SeperableKernel ker1 = KernelFactory.averageBlurKernel(3, EdgeMode.WRAP);
		 * InseperableKernel ker2 = ker1.toInseperable();
		 * 
		 * RGBImage img = TestUtils.safeScraper.nextImage().toRGB();
		 * 
		 * IImage<?> img1 = img.convolveWith(ker1); IImage<?> img2 =
		 * img.convolveWith(ker2);
		 * 
		 * ImageUtils.showImage(img, "Original"); ImageUtils.showImage(img1,
		 * "Seperable"); ImageUtils.showImage(img2, "Inseperable");
		 */

		@SuppressWarnings("resource")
		IImage<?> img = new SafebooruScraper().nextImage();
		
		
		ImageUtils.showImage(img.deepClone().resizeBilinear(32, 32).resizeNearest(512, 512), "32");
		ImageUtils.showImage(img.deepClone().resizeBilinear(40, 40).resizeNearest(512, 512), "40");
		ImageUtils.showImage(img.deepClone().resizeBilinear(64, 64).resizeNearest(512, 512), "64");
		ImageUtils.showImage(img.deepClone().resizeBilinear(128, 128).resizeNearest(512, 512), "128");
		
		/*
		 * SafebooruScraper s = new SafebooruScraper(); int found = 0; while (found <
		 * 100) { SourcedImage img = s.nextImage(); if (img.getWidth() ==
		 * img.getHeight()) { ImageUtils.showImage(img); } } s.close();
		 */

	}

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

	// Also for testing Pipeline
	/*
	 * IHashAlgorithm dhash = new DifferenceHash(); ImageOperator op = new
	 * ImageOperator(new SafebooruScraper(), 25, (SourcedImageOperation) (img) -> {
	 * System.out.println(dhash.hash(img)); return img; }); op.invokeAll();
	 * op.close();
	 */
}
