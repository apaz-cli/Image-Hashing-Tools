package hash;

import java.util.Arrays;

// For validation:
// 43FED13DAB04CA4789A799CA39767B4A955DE6E6B4C238F363A662666BCE912B,pHash,32,https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.cmc.edu%2Fsites%2Fdefault%2Ffiles%2Fnews%2F2013%2F05%2Flena.jpg&f=1&nofb=1

public class HalfDCTII {

	// This is separate because it should be stored with pHash's IHashAlgorithm, so
	// that it doesn't having to recreate the weights every time hash() is called.
	public static double[] createHalfDCTIICoefficients(int originalSize) {
		int halfRoundedDown = (originalSize / 2);
		double[] DCTCoefficients = new double[halfRoundedDown];

		Arrays.fill(DCTCoefficients, 1);
		DCTCoefficients[0] = 1 / Math.sqrt(2.0);

		return DCTCoefficients;
	}

	// This transform is uninvertible, and not useful for compression. This is a
	// DCTII for use in PerceptualHash#hash(). The algorithm requires a discrete
	// cosine transform, but only uses the upper-left corner. Therefore, we can make
	// the process more efficient by just not computing the rest of it. Another
	// optimization is to precompute a table of cosine values to avoid duplicate
	// calls, and to collapse that into a single array by combining indices. We use
	// this technique whenever possible.
	public static double[] halfDCTII(byte[] original, int size, double[] DCTCoefficients) {

		int halfRoundedDown = (size / 2);

		double[] cosineLookup = new double[halfRoundedDown * size];
		for (int uv = 0; uv < halfRoundedDown; uv++) {
			for (int ij = 0; ij < size; ij++) {
				cosineLookup[uv * size + ij] = Math.cos(((2 * ij + 1) / (2.0 * size)) * uv * Math.PI);
			}
		}

		double[] transformed = new double[halfRoundedDown * halfRoundedDown];

		for (int u = 0; u < halfRoundedDown; u++) {
			for (int v = 0; v < halfRoundedDown; v++) {
				double sum = 0.0;
				for (int i = 0; i < size; i++) {
					for (int j = 0; j < size; j++) {
						sum += cosineLookup[u * size + i] * cosineLookup[v * size + j] * original[i * size + j];
					}
				}
				sum *= (((2 * DCTCoefficients[u] * DCTCoefficients[v]) / size));
				transformed[u * halfRoundedDown + v] = sum;
			}
		}

		return transformed;
	}

}
