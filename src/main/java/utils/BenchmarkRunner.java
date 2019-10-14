package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
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
import image.*;
import image.implementations.*;
import match.downloader.ImageDownloader;
import match.hasher.ImageHasher;
import attack.implementations.*;

@SuppressWarnings("unused")
public class BenchmarkRunner {

	private static BufferedImage img1 = null;
	private static BufferedImage img2 = null;
	private static String image1URL = "https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png";
	private static String image2URL = "https://pbs.twimg.com/media/D8s6grBU0AAADD3?format=jpg&name=medium";

	static {
		try {
			img1 = ImageUtils.openImage(new URL(image1URL));
			System.out.println("Image 1 width: " + img1.getWidth());
			System.out.println("Image 1 height: " + img1.getHeight());

			img2 = ImageUtils.openImage(new URL(image2URL));
			System.out.println("Image 2 width: " + img2.getWidth());
			System.out.println("Image 2 height: " + img2.getHeight());

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int warmupIterations = 1000;

	// ********//
	// * MAIN *//
	// ********//

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		showImage(img1);
		showImage(new RGBImage(img1));
		showImage(new GreyscaleImage(img1));
		showImage(new YCbCrImage(img1).getY());
		
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

		// Paste code to benchmark here.
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
		// Paste code to benchmark here.

		RGBImage rgb = new RGBImage(img1);
		GreyscaleImage gsi = rgb.toGreyscale();
		gsi = gsi.resizeBilinear(1000, 1000);

		long time2 = System.currentTimeMillis();
		return (time2 - time1);
	}

	private static long timeContestant2() {

		long time1 = System.currentTimeMillis();
		// Paste code to benchmark here.

		RGBImage rgb = new RGBImage(img1);
		rgb = rgb.resizeBilinear(1000, 1000);
		GreyscaleImage gsi = rgb.toGreyscale();

		long time2 = System.currentTimeMillis();
		return (time2 - time1);
	}

}
