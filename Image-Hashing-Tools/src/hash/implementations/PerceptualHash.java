package hash.implementations;

import java.awt.image.BufferedImage;

import hash.ComparisonType;
import hash.DCTUtils;
import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.PixelUtils;
import image.implementations.RGBImage;

public class PerceptualHash implements IHashAlgorithm {

	private int size;
	private double[] DCTCoefficients;

	public PerceptualHash() {
		this(32);
	}

	public PerceptualHash(int sideLength) {
		if (PixelUtils.safeSquare(sideLength) % 2 != 0)
			throw new IllegalArgumentException("sideLength must be even.");
		this.size = sideLength;
		this.DCTCoefficients = DCTUtils.createHalfDCTCoefficients(sideLength);
	}

	@Override
	public ComparisonType getComparisonType() {
		return ComparisonType.HAMMING;
	}

	@Override
	public String getHashName() {
		return "pHash";
	}

	@Override
	public int getHashLength() {
		int trimmedSize = (this.size / 2);
		return trimmedSize * trimmedSize;
	}

	@Override
	public String serialize() {
		return "" + this.size;
	}

	@Override
	public IHashAlgorithm deserialize(String serialized) throws IllegalArgumentException {
		try {
			return new PerceptualHash(Integer.parseInt(serialized.trim()));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Expected one integer value.");
		}
	}

	@Override
	public boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode) {
		hash1.assertComparable(hash2);
		if (!hash1.getHashInformation().contentEquals(this.getHashInformation())) {
			throw new IllegalArgumentException(
					"The hash information in this hash does not match the information that this IHashAlgorithm would produce. Expected: "
							+ this.getHashInformation() + "got:" + hash1.getHashInformation() + ".");
		}

		// Doubles are represented exactly for a very large number of bits, so this is
		// okay.
		double dist = hash1.distance(hash2);
		if (mode == MatchMode.SLOPPY) {
			return dist < (8 / (double) 64) * hash1.getHashLength();
		} else if (mode == MatchMode.NORMAL) {
			return dist < (5 / (double) 64) * hash1.getHashLength();
		} else if (mode == MatchMode.STRICT) {
			return dist < (2 / (double) 64) * hash1.getHashLength();
		} else if (mode == MatchMode.EXACT) {
			return dist == 0;
		} else {
			throw new IllegalArgumentException("Invalid MatchMode: " + mode);
		}
	}

	@Override
	public ImageHash hash(IImage<?> img) {
		// Algorithm summary can be found here:
		// http://hackerfactor.com/blog/index.php%3F/archives/432-Looks-Like-It.html
		// And also check out phash.org

		// Function chain avoids keeping unnecessary references.
		// Pack the image into a double array. Then, apply DCT and trim.
		// @nof
		double[][] transformedTrimmedDCT = 
				DCTUtils.halfDCTII(
				packPixels(img, this.size),
				this.size, DCTCoefficients);
		// @dof

		// Now we put the bits of the hash into a long[], and make an ImageHash object
		// out of it.
		return new ImageHash(this, constructHash(transformedTrimmedDCT), this.findSource(img));
	}

	// Resize the image, and convert it to a 2d array of doubles.
	private static double[][] packPixels(IImage<?> img, int size) {
		byte[] bpixels = img.resizeBilinear(size, size).toGreyscale().getPixels();
		double[][] dpixels = new double[size][size];

		int offset = 0;
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++)
				dpixels[y][x] = (double) bpixels[offset++];

		return dpixels;
	}

	private static long[] constructHash(double[][] transformedTrimmedDCT) {

		// First, calculate the mean value of the whole thing. Doing so in batches
		// allows for much smaller average loss in precision.
		int trimmedSize = transformedTrimmedDCT.length;
		double meanValue;
		{
			double[] rowAverages = new double[trimmedSize];
			for (int y = 0; y < trimmedSize; y++) {
				rowAverages[y] = avg(transformedTrimmedDCT[y]);
			}
			meanValue = avg(rowAverages);
		}

		// Now set the bits of the hash.
		long[] hashValues = new long[((trimmedSize * trimmedSize) + 63) / 64];

		int longPos = 0, currentLong = -1;
		for (int y = 0; y < trimmedSize; y++) {
			for (int x = 0; x < trimmedSize; x++) {

				if (longPos % 64 == 0) {
					// -1 will immediately be incremented to 0, and it will spill over into new
					// longs when necessary.
					currentLong++;
					longPos = 0;
				}

				// Set the current bit of the hash
				hashValues[currentLong] <<= 1;
				hashValues[currentLong] |= transformedTrimmedDCT[y][x] > meanValue ? 1 : 0;
				longPos++;
			}
		}

		// Slide the last entry of the hash in to the left if necessary.
		hashValues[currentLong] <<= 64 - longPos;

		return hashValues;
	}

	private static double avg(double[] arr) {
		double total = 0;
		for (double d : arr) {
			total += d;
		}
		return total / arr.length;
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		return this.hash(new RGBImage(img));
	}

}
