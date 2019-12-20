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
import image.*;
import image.implementations.*;
import pipeline.hasher.ImageHasher;
import pipeline.sources.*;
import pipeline.sources.impl.*;
import pipeline.sources.impl.buffer.ImageBuffer;
import pipeline.sources.impl.downloader.URLCollectionDownloader;
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

	// ********//
	// * MAIN *//
	// ********//

	static IHashAlgorithm dHash = new DifferenceHash();
	static SourcedImage lastImage = null;
	static {
		try {
			lastImage = new SourcedImage(img1, new URL(image1URL));
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {

		URLCollectionDownloader s = null;
		try {
			s = new URLCollectionDownloader(new File("C:\\Users\\PazderaAaron\\Downloads\\Attachment_Links.txt"));
			System.out.println("Done Constructing");
		} catch (IOException e) {
			e.printStackTrace();
		}

		SourcedImageOperation compareDifferentImageDifference = (img) -> {

			ImageHash h1 = dHash.hash(lastImage);
			ImageHash h2 = dHash.hash(img);
			float diff = h1.percentHammingDifference(h2);
			// System.out.println(diff + (diff < .20 ? "\nMATCH:\n" + h1.getSource() + "\n" + h2.getSource() : ""));
			lastImage = img;
			return img;
		};

		ImageOperator operator = new ImageOperator(s, compareDifferentImageDifference);
		System.out.println("Executing All");
		operator.executeAll();
		System.out.println("All Executed");
		System.out.println(s.getFailedDownloads());
		operator.close();
		System.out.println("closed");

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

	

}
