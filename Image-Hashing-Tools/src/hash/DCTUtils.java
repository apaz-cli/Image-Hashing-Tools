package hash;

public class DCTUtils {

	// https://wikimedia.org/api/rest_v1/media/math/render/svg/dce6d60796ea026a5a7564418d130effde90d9cf

	// TODO Optimize by hardcoding for specific cases, such as size = 8, 16, 32.

	public static double[] createHalfDCTCoefficients(int originalSize) {
		int halfRoundedUp = originalSize / 2;
		halfRoundedUp = (originalSize & 0x1) == 1 ? halfRoundedUp + 1 : halfRoundedUp;
		return createDCTCoefficients(halfRoundedUp);
	}

	// This transform is uninvertible, use the IDCTII method instead of this one if
	// you want to be able to invert back. So, this method isn't useful for
	// compression. However, this method is exactly what is required for pHash.
	public static double[][] halfDCTII(double[][] original, int size, double[] DCTCoefficients) {
		int halfRoundedUp = size / 2;
		halfRoundedUp = (size & 0x1) == 1 ? halfRoundedUp + 1 : halfRoundedUp;

		double[][] transformed = new double[halfRoundedUp][halfRoundedUp];
		for (int u = 0; u < halfRoundedUp; u++) {
			for (int v = 0; v < halfRoundedUp; v++) {
				double sum = 0.0;
				for (int i = 0; i < size; i++) {
					for (int j = 0; j < size; j++) {
						// @nof
							sum += Math.cos(((2 * i + 1) / (2.0 * size)) * u * Math.PI)
								 * Math.cos(((2 * j + 1) / (2.0 * size)) * v * Math.PI) 
								 * original[i][j];
							// @dof
					}
				}
				sum *= (((2 * DCTCoefficients[u] * DCTCoefficients[v]) / size));
				transformed[u][v] = sum;
			}
		}
		return transformed;
	}

	// This is separate because it should be stored with the IHashAlgorithm, so that
	// it doesn't having to recreate the weights.
	public static double[] createDCTCoefficients(int size) {
		double[] DCTCoefficients = new double[size];
		DCTCoefficients[0] = 1 / Math.sqrt(2.0);
		for (int i = 1; i < size; i++) {
			DCTCoefficients[i] = 1;
		}
		return DCTCoefficients;
	}

	public static double[][] DCTII(double[][] original, int size, double[] DCTCoefficients) {
		double[][] transformed = new double[size][size];
		for (int u = 0; u < size; u++) {
			for (int v = 0; v < size; v++) {
				double sum = 0.0;
				for (int i = 0; i < size; i++) {
					for (int j = 0; j < size; j++) {
						// @nof
						sum += Math.cos(((2 * i + 1) / (2.0 * size)) * u * Math.PI)
							 * Math.cos(((2 * j + 1) / (2.0 * size)) * v * Math.PI) 
							 * original[i][j];
						// @dof
					}
				}
				sum *= (((2 * DCTCoefficients[u] * DCTCoefficients[v]) / size));
				transformed[u][v] = sum;
			}
		}
		return transformed;
	}

	public static double[][] IDCTII(double[][] transformed, int size, double[] DCTCoefficients) {
		double[][] original = new double[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				double sum = 0.0;
				for (int u = 0; u < size; u++) {
					for (int v = 0; v < size; v++) {
						// @nof
						sum +=  ((2 * DCTCoefficients[u] * DCTCoefficients[v]) / size)
								* Math.cos(((2 * i + 1) / (2.0 * size)) * u * Math.PI)
								* Math.cos(((2 * j + 1) / (2.0 * size)) * v * Math.PI) 
								* transformed[u][v];
						// @dof
					}
				}
				original[i][j] = Math.round(sum);
			}
		}
		return original;
	}

}
