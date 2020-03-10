package attack.convolutions;

import java.util.Arrays;

import image.PixelUtils;

public class KernelFactory {

	public static SeperableKernel averageBlurKernel(int sideLength, EdgeMode mode) {
		ConvolutionKernel.assertOdd(sideLength);
		int pixelNum = PixelUtils.safeSquare(sideLength);

		float[] kernel = new float[pixelNum];
		Arrays.fill(kernel, 1f / sideLength);

		return new SeperableKernel(kernel, kernel, mode);
	}

	public static InseperableKernel gaussianBlurKernel(int sideLength, float blurFactor) {
		ConvolutionKernel.assertOdd(sideLength);
		int center = (sideLength / 2) + 1;

		// https://en.wikipedia.org/wiki/Gaussian_blur

		float[][] kernel = new float[sideLength][sideLength];
		{
			float twoTimesBlurSquared = blurFactor * blurFactor * 2f;
			int xdistance, ydistance;
			double exponent, scalar = 1d / (Math.PI * twoTimesBlurSquared);
			for (int y = 0; y < sideLength; y++) {
				for (int x = 0; x < sideLength; x++) {

					xdistance = Math.abs(center - x);
					ydistance = Math.abs(center - y);
					exponent = -((xdistance * xdistance) + (ydistance * ydistance)) / (twoTimesBlurSquared);

					kernel[y][x] = (float) (scalar * Math.pow(Math.E, exponent));
				}
			}
		}

		return new InseperableKernel(kernel, sideLength);
	}
}
