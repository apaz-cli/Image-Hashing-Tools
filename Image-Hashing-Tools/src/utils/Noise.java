package utils;

import java.util.Random;

import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import image.implementations.RGBImage;

public class Noise {
	// Returns an image packed with noise 0-255
	public static GreyscaleImage noise(int width, int height) {
		Random r = new Random();
		byte[] noise = new byte[width * height];
		r.nextBytes(noise);
		return new GreyscaleImage(noise, width, height);
	}

	public static RGBImage noiseRGB(int width, int height) {
		return new RGBImage(noise(width, height), noise(width, height), noise(width, height));
	}

	public static RGBAImage noiseRGBA(int width, int height) {
		return new RGBAImage(noise(width, height), noise(width, height), noise(width, height), noise(width, height));
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
}
