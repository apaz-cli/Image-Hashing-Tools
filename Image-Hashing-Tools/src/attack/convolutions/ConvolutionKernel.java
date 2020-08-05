package attack.convolutions;

import attack.IAttack;
import image.IImage;
import image.PixelUtils;

public interface ConvolutionKernel<T extends IImage<? extends T>> extends IAttack<T> {

	abstract public float[] getKernel();

	abstract public int getSideLength();

	abstract public EdgeMode getMode();

	
	@Override
	default public T apply(T img) {
		return this.apply(img, 1);
	}

	default T apply(T img, int iterations)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (iterations < 0) { throw new IllegalArgumentException("Number of Iterations cannot be less than zero."); }

		for (int i = 0; i < iterations; i++) {
			img = this.apply(img);
		}
		return img;
	}

	default public float[][] getKernel2d() {
		int sideLength = this.getSideLength();
		return PixelUtils.array1dToArray2d(this.getKernel(), sideLength, sideLength);
	}

	static void assertOdd(int n) {
		if ((n & 0x1) == 0) { throw new IllegalArgumentException("Cannot be even."); }
	}

	static float[] normalizeKernel(float[] kernel) {
		float sum = 0;
		for (int i = 0; i < kernel.length; i++) {
			sum += kernel[i];
		}

		float normalizationWeight = 1f / sum;

		float[] normalizedKernel = new float[kernel.length];
		for (int i = 0; i < kernel.length; i++) {
			normalizedKernel[i] = kernel[i] * normalizationWeight;
		}

		return normalizedKernel;
	}

	static float[][] normalizeKernel2d(float[][] kernel, int sideLength) {
		float sum = 0;
		for (int y = 0; y < sideLength; y++) {
			for (int x = 0; x < sideLength; x++) {
				sum += kernel[y][x];
			}
		}

		float normalizationWeight = 1f / sum;

		float[][] normalizedKernel = new float[sideLength][sideLength];
		for (int y = 0; y < sideLength; y++) {
			for (int x = 0; x < sideLength; x++) {
				normalizedKernel[y][x] = kernel[y][x] * normalizationWeight;
			}
		}

		return normalizedKernel;
	}

}
