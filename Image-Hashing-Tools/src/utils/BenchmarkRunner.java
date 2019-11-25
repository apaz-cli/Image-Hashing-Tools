package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import hash.*;
import hash.implementations.*;
import hash.implementations.slicehash.*;
import image.*;
import image.implementations.*;
import pipeline.hasher.ImageHasher;
import pipeline.sources.*;
import pipeline.sources.impl.*;
import pipeline.sources.impl.loader.ImageLoader;
import pipeline.sources.ops.IImageOperation;
import pipeline.sources.ops.ImageOperator;
import pipeline.sources.ops.SourcedImageOperation;
import attack.IAttack;
import attack.implementations.*;

@SuppressWarnings("unused")
public class BenchmarkRunner {

	private static BufferedImage img1 = null;
	private static BufferedImage img2 = null;
	private static String image1URL = "https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png";
	private static String image2URL = "https://images3.alphacoders.com/836/83635.jpg"; // "https://safebooru.org//images/2824/c7f88eef1dda8cf4a5d06c6f732da9e14d08fb38.png";//"https://pbs.twimg.com/media/D8s6grBU0AAADD3?format=jpg&name=medium";
	/*
	 * static { try { img1 = ImageUtils.openImage(new URL(image1URL));
	 * System.out.println("Image 1 width: " + img1.getWidth());
	 * System.out.println("Image 1 height: " + img1.getHeight());
	 * 
	 * img2 = ImageUtils.openImage(new URL(image2URL));
	 * System.out.println("Image 2 width: " + img2.getWidth());
	 * System.out.println("Image 2 height: " + img2.getHeight());
	 * 
	 * } catch (MalformedURLException e) { e.printStackTrace(); } catch (IOException
	 * e) { e.printStackTrace(); } }
	 */
	private static int warmupIterations = 1000;

	// ********//
	// * MAIN *//
	// ********//

	static DifferenceHash dhash = new DifferenceHash();

	public static void main(String[] args) {
		
		ImageSource s = new ImageLoader("C:\\Users\\PazderaAaron\\Wallpapers");

		SourcedImageOperation compareResizeHashes = (img) -> {
			SourcedImage copy = new SourcedImage(img.deepCopy().unwrap().rescaleBilinear(.5f, .5f), img.getSource(), img.isURL());
			ImageHash h1 = dhash.hash(img);
			ImageHash h2 = dhash.hash(copy);
			float diff = h1.percentHammingDifference(h2);
			System.out.println(diff >= .25f ? diff + " " + img.getSource() : diff);
			if (diff >= .25f) {
				showImage(img);
				showImage(copy);
			}
			return img;
		};

		ImageOperator operator = new ImageOperator(s, compareResizeHashes);
		operator.executeAll();
		operator.close();
		s.close();
		System.out.println("Closed");
		

		/*
		 * int[][] testarr = ImageUtils.array1dToArray2d(new int[] {1, 2, 3, 4, 5, 6},
		 * 3, 2); for (int i = 0; i < testarr.length; i++) {
		 * System.out.println(Arrays.toString(testarr[i])); }
		 * 
		 * testarr = ImageUtils.transpose(testarr); for (int i = 0; i < testarr.length;
		 * i++) { System.out.println(Arrays.toString(testarr[i])); }
		 * 
		 * testarr = ImageUtils.transpose(testarr); for (int i = 0; i < testarr.length;
		 * i++) { System.out.println(Arrays.toString(testarr[i])); }
		 * 
		 * System.out.println(Arrays.toString(ImageUtils.array2dToArray1d(testarr, 2,
		 * 3)));
		 * 
		 * RGBImage img = new RGBImage(img2); showImage(img.resizeBilinear(32,
		 * 32).resizeNearest(512, 512), "Original"); GreyscaleImage grey =
		 * img.resizeBilinear(32, 32).toGreyscale(); showImage(grey.resizeNearest(512,
		 * 512), "Resized"); SliceHash sHash = new SliceHash(); sHash.hash(grey);
		 */

		/*
		 * if (args.length == 0) { args = new String[] {
		 * "C:\\Users\\PazderaAaron\\Downloads\\Attachment_Links.txt",
		 * "C:\\Users\\PazderaAaron\\Downloads\\AttachmentHashes.txt" }; }
		 * 
		 * try { ImageHasher hasher = new ImageHasher(new File(args[0]), new
		 * DifferenceHash(), new File(args[1])); hasher.awaitCompletion(); for (String s
		 * : hasher.getFailedDownloadURLs()) { System.err.println(s); } } catch
		 * (IOException | InterruptedException | ExecutionException e) {
		 * e.printStackTrace(); }
		 */

		/*
		 * ImageIO.write(new RGBImage(img1).toBufferedImage(), "png", new
		 * File("lena.png")); ImageIO.write(new RGBImage(img1).resizeNearest(9,
		 * 8).rescaleNearest(25, 25).toBufferedImage(), "png", new
		 * File("lena 9x8.png")); ImageIO.write(new RGBImage(img1).resizeNearest(9,
		 * 8).toGreyscale().rescaleNearest(25, 25).toBufferedImage(), "png", new
		 * File("lena 9x8 greyscale.png"));
		 * ImageIO.write(ImageUtils.imageRepresentation(new
		 * DifferenceHash().hash(img1)).rescaleNearest(25, 25).toBufferedImage(), "png",
		 * new File("lena dhash.png"));
		 */
	}

	public static void showImage(SourcedImage img) {
		showImage(img.unwrap());
	}

	public static void showImage(IImage<?> img) {
		showImage(img.toBufferedImage(), "");
	}

	public static void showImage(IImage<?> img, String name) {
		showImage(img.toBufferedImage(), name);
	}

	public static void showImage(BufferedImage img) {
		showImage(img, "");
	}

	public static void showImage(BufferedImage img, String name) {
		JFrame editorFrame = new JFrame(name);
		editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		ImageIcon imageIcon = new ImageIcon(img);
		JLabel jLabel = new JLabel();
		jLabel.setIcon(imageIcon);
		editorFrame.getContentPane().add(jLabel, BorderLayout.CENTER);

		editorFrame.pack();
		editorFrame.setLocationRelativeTo(null);
		editorFrame.setVisible(true);
	}

	private static void averageBenchmark(int iterations) {
		// Load Class
		for (int i = 0; i < warmupIterations; i++) {
			benchmark();
		}

		double totalTime = 0d;
		for (int it = 0; it < iterations; it++) {
			totalTime += benchmark();
		}
		System.out.println("Completed in " + totalTime / (double) iterations + "ms on average.");
	}

	private static long benchmark() {
		long time1 = System.currentTimeMillis();

		// Paste code to pipeline here.
		IHashAlgorithm alg = new AverageHash();
		boolean match = alg.matches(alg.hash(img1), alg.hash(img2), MatchMode.SLOPPY);

		long time2 = System.currentTimeMillis();
		return (time2 - time1);
	}

	private static void race(int iterations, String contestant1Name, String contestant2Name) {
		// Load classes
		raceWarmup();
		System.out.println("Warmed up.");

		// Time the first block
		double totalTime = 0d;
		for (int it = 0; it < iterations; it++) {
			totalTime += timeContestant1();
		}
		double contestant1 = totalTime / (double) iterations;
		System.out.println("Contestant 1 finished in " + contestant1 + "ms on average.");

		// Time the second block
		totalTime = 0d;
		for (int it = 0; it < iterations; it++) {
			totalTime += timeContestant2();
		}
		double contestant2 = totalTime / (double) iterations;
		System.out.println("Contestant 2 finished in " + contestant2 + "ms on average.");

		// Compare
		DecimalFormat df = new DecimalFormat("#.00");
		if (contestant1 > contestant2) {
			System.out.println(contestant1Name + " executed " + df.format(contestant1 - contestant2)
					+ "ms faster on average. (" + df.format(contestant1 / contestant2) + "x faster)");
			return;
		}
		System.out.println(contestant2Name + " executed " + df.format(contestant2 - contestant1)
				+ "ms faster on average. (" + df.format(contestant2 / contestant1) + "x faster)");
		return;
	}

	private static void raceWarmup() {
		for (int i = 0; i < warmupIterations; i++) {
			timeContestant1();
			timeContestant2();
		}
	}

	private static long timeContestant1() {

		long time1 = System.currentTimeMillis();
		// Paste code to pipeline here.

		RGBImage rgb = new RGBImage(img1);
		GreyscaleImage gsi = rgb.toGreyscale();
		gsi = gsi.resizeBilinear(1000, 1000);

		long time2 = System.currentTimeMillis();
		return (time2 - time1);
	}

	private static long timeContestant2() {

		long time1 = System.currentTimeMillis();
		// Paste code to pipeline here.

		RGBImage rgb = new RGBImage(img1);
		rgb = rgb.resizeBilinear(1000, 1000);
		GreyscaleImage gsi = rgb.toGreyscale();

		long time2 = System.currentTimeMillis();
		return (time2 - time1);
	}

}
