package attack.convolutions;

import attack.IAttack;
import image.PixelUtils;

public interface ConvolutionKernel extends IAttack {

	abstract public float[] getKernel();

	abstract public int getSideLength();
	
	abstract public EdgeMode getMode();

	default public float[][] getKernel2d() {
		int sideLength = this.getSideLength();
		return PixelUtils.array1dToArray2d(this.getKernel(), sideLength, sideLength);
	}

	
	static void assertOdd(int n) {
		if ((n & 0x1) == 0) {
			throw new IllegalArgumentException("Cannot be even.");
		}
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
