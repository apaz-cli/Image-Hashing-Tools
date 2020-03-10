package utils;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import hash.ImageHash;
import image.IImage;
import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import image.implementations.RGBImage;
import image.implementations.SourcedImage;

public class ImageUtils {

	protected ImageUtils() {
	}

	// Returns null if isn't an image, or it's a gif
	public static BufferedImage openImage(URL imgURL) throws IOException {

		// Due to a Java 8 bug in ImageIO, cannot read animated gifs.
		if (imgURL.getPath().contains(".gif")) {
			return null;
		}

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

		InputStream s = connection.getInputStream();
		return ImageIO.read(s);
	}

	public static BufferedImage openImage(File imgFile) throws IOException {
		if (!imgFile.isFile()) {
			throw new IllegalArgumentException("imgFile is not a file.");
		}
		if (!imgFile.canRead()) {
			throw new IllegalArgumentException("Insufficient permission to read file.");
		}
		
		// Cannot read gifs due to ImageIO bug.
		return imgFile.toString().endsWith(".gif") ? null : ImageIO.read(imgFile);
	}

	public static SourcedImage openImageSourced(URL imgURL) throws IOException {
		BufferedImage img = openImage(imgURL);
		return img == null ? null : new SourcedImage(img, imgURL);
	}

	public static SourcedImage openImageSourced(File imgFile) throws IOException {
		BufferedImage img = openImage(imgFile);
		return img == null ? null : new SourcedImage(img, imgFile);
	}

	public static void showImage(SourcedImage img) {
		showImage(img.unwrap(), img.getSource());
	}

	public static void showImage(IImage<?> img) {
		showImage(img.toBufferedImage(), (img instanceof SourcedImage) ? ((SourcedImage) img).getSource() : "");
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

	public static GreyscaleImage imageRepresentation(ImageHash hash, int width, int height)
			throws IllegalArgumentException {
		return imageRepresentation(hash.getBitArray(), width, height);
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
