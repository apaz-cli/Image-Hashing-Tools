package hash;

public class HashUtils {
	public static int hammingDistance(long[] bits1, long[] bits2) {
		if (bits1.length != bits2.length) throw new IllegalArgumentException(
				"Bit arrays are not the same length. Got: " + bits1.length + " and " + bits2.length + ".");
		long b;
		int distance = 0;
		for (int idx = 0; idx < bits1.length; idx++) {
			b = bits1[idx] ^ bits2[idx];
			b -= (b >> 1) & 0x5555555555555555L;
			b = (b & 0x3333333333333333L) + ((b >> 2) & 0x3333333333333333L);
			b = (b + (b >> 4)) & 0x0f0f0f0f0f0f0f0fL;
			distance += (b * 0x0101010101010101L) >> 56;
		}
		return distance;
	}

	public static double euclideanF32Distance(float[] bits1, float[] bits2) {
		if (bits1.length != bits2.length) throw new IllegalArgumentException(
				"Bit arrays are not the same length. Got: " + bits1.length + " and " + bits2.length + ".");
		double product = 0;
		for (int i = 0; i < bits1.length; i++) {
			double diff = bits1[i] - bits2[i];
			product += diff * diff;
		}
		return Math.sqrt(product);
	}

	public static double euclideanF64Distance(double[] bits1, double[] bits2) {
		if (bits1.length != bits2.length) throw new IllegalArgumentException(
				"Bit arrays are not the same length. Got: " + bits1.length + " and " + bits2.length + ".");
		double product = 0;
		for (int i = 0; i < bits1.length; i++) {
			double diff = bits1[i] - bits2[i];
			product += diff * diff;
		}
		return Math.sqrt(product);
	}
}
