package utils;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import hash.ImageHash;
import image.IImage;
import image.PixelUtils;
import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import image.implementations.RGBImage;
import image.implementations.SourcedImage;

public class ImageUtils {

	protected ImageUtils() {}

	public static boolean validURL(String s) {
		// Hopefully it's better than doing this. I don't wanna do this.
		/*
		 * try { new URL(s); return true; } catch (MalformedURLException e) { return
		 * false; }
		 */

		// This is PROBABLY an acceptable way to do it. The character / happens to be
		// invalid in a file name in all of UNIX, MacOS, and Windows. And I don't think
		// that there's a way to get that combination of characters. Furthermore, each
		// URL is going to start with http:// or https://
		return s.contains("://");
	}

	public static List<Exception> failedOpens = new Vector<>();

	// Returns null if isn't an image, or it's a gif
	public static BufferedImage openImage(URL imgURL) {
		try {
			// Due to a Java 8 bug in ImageIO, cannot read animated gifs.
			// if (imgURL.getPath().contains(".gif")) { return null; }

			final HttpURLConnection connection = (HttpURLConnection) imgURL.openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

			int code = connection.getResponseCode();
			if (code == 403) {
				System.err.println("Error 403: Forbidden for: " + imgURL);
				return null;
			} else if (code == 404) {
				System.err.println("Error 404: Not Found for: " + imgURL);
				return null;
			} else if (code < 500 && code >= 400) {
				System.err.println("Client error (" + code + ") for: " + imgURL);
				return null;
			} else if (code < 600 && code >= 500) {
				System.err.println("Server error (" + code + ") for: " + imgURL);
				return null;
			}

			InputStream s = new BufferedInputStream(connection.getInputStream());
			return ImageIO.read(s);
		} catch (Exception e) {
			failedOpens.add(e);
			return null;
		}

	}

	public static BufferedImage openImage(File imgFile) throws IOException {
		BufferedImage ret = null;
		try {
			if (!imgFile.isFile()) throw new IllegalArgumentException("imgFile is not a file. File: " + imgFile);
			if (!imgFile.canRead())
				throw new IllegalArgumentException("Insufficient permission to read file: " + imgFile);
			ret = ImageIO.read(imgFile);
		} catch (Exception e) {
			failedOpens.add(e);
		}
		return ret;
	}

	public static SourcedImage openImageSourced(URL imgURL) throws IOException {
		BufferedImage img = openImage(imgURL);
		return img == null ? null : new SourcedImage(img, imgURL);
	}

	public static SourcedImage openImageSourced(File imgFile) throws IOException {
		BufferedImage img = openImage(imgFile);
		return img == null ? null : new SourcedImage(img, imgFile);
	}

	/************************/
	/* IMAGE SAVING HELPERS */
	/************************/

	private static Set<String> suffixes = new HashSet<>(Arrays.asList(ImageIO.getWriterFileSuffixes()));

	private static String formatName(String name) {
		int idx = name.lastIndexOf('.');
		if (idx > 0) {
			return name.substring(idx + 1);
		} else {
			return "";
		}
	}

	public static boolean formatSupported(String formatName) { return suffixes.contains(formatName); }

	public static String formatName(File f) { return formatName(f.toString()); }

	public static File avoidNameCollision(File f) {
		PixelUtils.assertNotNull(f);

		String name = f.getName();
		String formatName = ImageUtils.formatName(f);

		return avoidNameCollision(name, formatName);
	}

	private static File avoidNameCollision(String name, String formatName) {
		PixelUtils.assertNotNull(name, formatName);
		if (formatName.startsWith(".") && formatName.length() > 1) formatName = formatName.substring(1);
		if (!formatSupported(formatName)) throw new IllegalArgumentException(
				"File format " + formatName + " not supported. Supported formats: " + suffixes);
		return avoidNameCollision(name, formatName, false);
	}

	// Only called recursively from above
	private static File avoidNameCollision(String name, String formatName, boolean changed) {

		int i = name.lastIndexOf('.');
		if (i > 0) name = name.substring(0, i);

		File f = new File(name + "." + formatName);
		if (f.exists() || !f.isDirectory()) return f;

		if (!changed) return avoidNameCollision(name + " (1)." + formatName, formatName, true);
		else {
			String beforeNumber = null;
			long number;
			String afterNumber = null;

			i = name.lastIndexOf('(');
			if (i > 0) {
				beforeNumber = name.substring(0, i + 1);
			}

			int j = name.lastIndexOf(')');
			if (j > 0) {
				afterNumber = name.substring(j, name.length());
			}

			number = Integer.parseInt(name.substring(i + 1, j));

			return avoidNameCollision(beforeNumber + (number + 1) + afterNumber, formatName, true);
		}
	}

	/****************/
	/* IMAGE SAVING */
	/****************/

	public static File saveImage(SourcedImage img) throws IOException {
		if (img == null) throw new IllegalArgumentException();
		File f = img.getIfFile();
		if (f == null) throw new IllegalArgumentException("The SourcedImage must be from a file.");
		return saveImage(img, f);
	}

	public static File saveImage(IImage<?> img, File f) throws IOException {
		if (img == null) throw new IllegalArgumentException();
		if (f == null) throw new IllegalArgumentException();
		return saveImage(img, f, ImageUtils.formatName(f));
	}

	public static File saveImage(IImage<?> img, File f, String format) throws IOException {
		if (img == null) throw new IllegalArgumentException();
		if (f == null) throw new IllegalArgumentException();
		if (format == null) throw new IllegalArgumentException();
		if (!ImageUtils.formatSupported(format))
			throw new IllegalArgumentException("Unsupported image format: " + format);

		if (f.isDirectory()) {
			Random r = new Random();
			int numbers;
			while ((numbers = r.nextInt()) < 0) {}

			String numberName = numbers + '.' + format;
			File newTarget = new File(f, numberName);

			// May or may not exist. This gets resolved on the next iteration below.
			saveImage(img, newTarget, format);
		}

		// We have a target and can just save
		if (!f.exists()) {
			ImageIO.write(img.toBufferedImage(), format, f);
			return f;
		} else {
			String parent = f.getParent();
			parent = parent == null ? "" : parent;

			File newTarget = ImageUtils.avoidNameCollision(f.getName(), format);

			ImageIO.write(img.toBufferedImage(), format, f);
			return newTarget;
		}
	}

	/****************************/
	/* SHOWING IMAGES ON SCREEN */
	/****************************/

	public static void showImage(SourcedImage img) {
		showImage(img.unwrap(), img.getSource());
	}

	public static void showImage(IImage<?> img) {
		showImage(img.toBufferedImage(), (img instanceof SourcedImage) ? ((SourcedImage) img).getSource() : "");
	}

	public static void showImage(IImage<?> img, String name) { showImage(img.toBufferedImage(), name); }

	public static void showImage(BufferedImage img) { showImage(img, ""); }

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

	public static GreyscaleImage imageRepresentation(ImageHash hash, int width, int height)
			throws IllegalArgumentException {
		return imageRepresentation(hash.bitsToLongArray(), width, height);
	}

	public static GreyscaleImage imageRepresentation(long[] hashBits, int width, int height)
			throws IllegalArgumentException {

		// Copy the array so that we don't screw up the original
		hashBits = Arrays.copyOfRange(hashBits, 0, hashBits.length);
		int pixelOffset = 0, hashIndex = 0;

		byte[] pixels = new byte[width * height];
		for (int i = 0; i < hashBits.length; i++) {
			for (int j = 0; j < 64; j++) {
				if (pixelOffset < width * height) {
					pixels[pixelOffset++] = (hashBits[hashIndex] & 0x1) == 0x1 ? (byte) 0 : (byte) 255;
					hashBits[hashIndex] >>= 1;
				}
			}
			hashIndex++;
		}

		return new GreyscaleImage(pixels, width, height);
	}

	// TODO Move these somewhere that makes more sense

	// Returns an image packed with noise 0-255
	public static GreyscaleImage noise(int width, int height) {
		Random r = new Random();
		byte[] noise = new byte[width * height];
		r.nextBytes(noise);
		return new GreyscaleImage(noise, width, height);
	}

	public static RGBImage noiseRGB(int width, int height) {
		return new RGBImage(ImageUtils.noise(width, height), ImageUtils.noise(width, height),
				ImageUtils.noise(width, height));
	}

	public static RGBAImage noiseRGBA(int width, int height) {
		return new RGBAImage(ImageUtils.noise(width, height), ImageUtils.noise(width, height),
				ImageUtils.noise(width, height), ImageUtils.noise(width, height));
	}

	// Returns an array containing normally distributed noise, rounded to the
	// nearest integer.
	public static float[] gaussianNoise(int length, float mean, float sd) {
		Random r = new Random();
		float[] gaussNoise = new float[length];
		for (int i = 0; i < length; i++) {
			gaussNoise[i] = (float) ((r.nextGaussian() * sd) + mean);
		}
		return gaussNoise;
	}

	public static BufferedImage resizeBI(BufferedImage img, int width, int height) {
		BufferedImage resized = new BufferedImage(width, height, img.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, width, height, 0, 0, img.getWidth(), img.getHeight(), null);
		g.dispose();
		return resized;
	}

}
