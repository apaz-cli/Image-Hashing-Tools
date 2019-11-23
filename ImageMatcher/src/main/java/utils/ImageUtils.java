package utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.BitSet;
import java.util.Random;

import javax.imageio.ImageIO;

import hash.ImageHash;
import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import image.implementations.RGBImage;

public class ImageUtils {

	protected ImageUtils() {}
	
	// Returns null if isn't an image, or it's a gif
	public static BufferedImage openImage(URL imgURL) throws IOException {

		// TODO Due to a Java 8 bug in ImageIO, cannot read animated gifs.
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

	public static GreyscaleImage imageRepresentation(ImageHash hash, int width, int height)
			throws IllegalArgumentException {
		return imageRepresentation(hash.getBits(), width, height);
	}

	public static GreyscaleImage imageRepresentation(ImageHash hash) throws IllegalArgumentException {
		return imageRepresentation(hash.getBits());
	}

	public static GreyscaleImage imageRepresentation(BitSet bs, int width, int height) throws IllegalArgumentException {
		if (bs.length() != width * height) {
			throw new IllegalArgumentException("The length of the BitSet must be equal to width * height. Expected: "
					+ bs.length() + " width * height was: " + width * height);
		}

		byte[] pixels = new byte[bs.length()];
		for (int i = 0; i < bs.length(); i++) {
			// Represent 1 as black, 0 as white
			pixels[i] = bs.get(i) == true ? (byte) 0 : (byte) 255;
		}

		return new GreyscaleImage(pixels, width, height);
	}

	public static GreyscaleImage imageRepresentation(BitSet bs) throws IllegalArgumentException {
		// Version without width, height arguments only works for perfect squares.
		double sqrt = Math.sqrt(bs.length());
		int rounded = (int) Math.round(Math.floor(sqrt));
		if (sqrt - rounded != 0) {
			throw new IllegalArgumentException(
					"The length of the BitSet must be a perfect square. Got: " + bs.length());
		}

		byte[] pixels = new byte[bs.length()];
		for (int i = 0; i < bs.length(); i++) {
			// Represent 1 as black, 0 as white
			pixels[i] = bs.get(i) == true ? (byte) 0 : (byte) 255;
		}

		return new GreyscaleImage(pixels, rounded, rounded);
	}

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
	public static int[] gaussianNoise(int length, int mean, int sd) {
		Random r = new Random();
		int[] gaussNoise = new int[length];
		for (int i = 0; i < length; i++) {
			gaussNoise[i] = (int) Math.round((r.nextGaussian() * sd) + mean);
		}
		return gaussNoise;
	}

	public static BufferedImage resize(BufferedImage img, int width, int height) {
		BufferedImage resized = new BufferedImage(width, height, img.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, width, height, 0, 0, img.getWidth(), img.getHeight(), null);
		g.dispose();
		return resized;
	}

	public static int[][] array1dToArray2d(int[] arr, int x, int y) {
		int[][] array2d = new int[y][x];

		int index = 0;
		for (int i = 0; i < array2d.length; i++) {
			for (int j = 0; j < array2d[i].length; j++) {
				array2d[i][j] = arr[index];
				index++;
			}
		}

		return array2d;
	}
	
	public static byte[][] array1dToArray2d(byte[] arr, int x, int y) {
		byte[][] array2d = new byte[y][x];

		int index = 0;
		for (int i = 0; i < array2d.length; i++) {
			for (int j = 0; j < array2d[i].length; j++) {
				array2d[i][j] = arr[index];
				index++;
			}
		}

		return array2d;
	}

	public static int[] array2dToArray1d(int[][] arr, int x, int y) {
		// arr[x][y]
		int[] array1d = new int[x * y];

		for (int i = 0; i < x; i++) {
			int[] currentArray = arr[i];
			if (currentArray.length != y) {
				throw new IllegalArgumentException("All subarrays of pixels must be of length equal to the width.");
			}

			for (int j = 0; j < y; j++) {
				array1d[i * y + j] = currentArray[j];
			}
		}

		return array1d;
	}

	

	public static byte[] array2dToArray1d(byte[][] arr, int x, int y) {
		// arr[x][y]
		byte[] array1d = new byte[x * y];

		for (int i = 0; i < x; i++) {
			byte[] currentArray = arr[i];
			if (currentArray.length != y) {
				throw new IllegalArgumentException("All subarrays of pixels must be of length equal to the width.");
			}

			for (int j = 0; j < y; j++) {
				array1d[i * y + j] = currentArray[j];
			}
		}

		return array1d;
	}
	
	public static byte[][] transpose(byte[][] matrix) {
		int m = matrix.length;
		int n = matrix[0].length;

		byte[][] transposedMatrix = new byte[n][m];

		for (int x = 0; x < n; x++) {
			for (int y = 0; y < m; y++) {
				transposedMatrix[x][y] = matrix[y][x];
			}
		}

		return transposedMatrix;
	}

	public static int[][] transpose(int[][] matrix) {
		int m = matrix.length;
		int n = matrix[0].length;

		int[][] transposedMatrix = new int[n][m];

		for (int x = 0; x < n; x++) {
			for (int y = 0; y < m; y++) {
				transposedMatrix[x][y] = matrix[y][x];
			}
		}

		return transposedMatrix;
	}

	// TODO actually implement transpose1d
	public static byte[] transpose1dAs2d(byte[] arr, int oldx, int oldy) {
		return array2dToArray1d(transpose(array1dToArray2d(arr, oldx, oldy)), oldy, oldx);
	}
	
	public static int[] transpose1dAs2d(int[] arr, int oldx, int oldy) {
		return array2dToArray1d(transpose(array1dToArray2d(arr, oldx, oldy)), oldy, oldx);
	}
	
}
