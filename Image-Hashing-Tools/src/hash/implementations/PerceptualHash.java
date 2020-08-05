package hash.implementations;

import java.awt.image.BufferedImage;

import hash.AlgLoader;
import hash.ComparisonType;
import hash.HalfDCTII;
import hash.HashUtils;
import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.PixelUtils;
import image.implementations.RGBImage;
import image.implementations.SourcedImage;

public class PerceptualHash implements IHashAlgorithm {

	static {
		PerceptualHash phash = new PerceptualHash();
		phash.DCTCoefficients = null; // Free unnecessary memory, as the alg does not need to actually work.
		AlgLoader.register(phash);
	}

	public PerceptualHash() {
		this(32);
	}

	public PerceptualHash(int sideLength) {
		if (PixelUtils.safeSquare(sideLength) % 2 != 0) throw new IllegalArgumentException("sideLength must be even.");
		this.sideLength = sideLength;
		this.DCTCoefficients = HalfDCTII.createHalfDCTIICoefficients(sideLength);
	}

	private int sideLength;
	private double[] DCTCoefficients;

	@Override
	public String algName() {
		return "pHash";
	}

	@Override
	public int getHashLength() { return (this.sideLength / 2) * (this.sideLength / 2); }

	@Override
	public ComparisonType getComparisonType() { return ComparisonType.HAMMING; }

	@Override
	public String toArguments() {
		return "" + this.sideLength;
	}

	@Override
	public IHashAlgorithm fromArguments(String serialized) throws IllegalArgumentException {
		try {
			return new PerceptualHash(Integer.parseInt(serialized.trim()));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Expected one integer value.");
		}
	}

	@Override
	public double distance(ImageHash hash1, ImageHash hash2) {
		if (this.canCompare(hash1, hash2)) {
			return HashUtils.hammingDistance(hash1.bitsToLongArray(), hash2.bitsToLongArray());
		} else throw new IllegalArgumentException("The chosen ");
	}

	@Override
	public boolean algEquals(IHashAlgorithm o) {
		if (o instanceof PerceptualHash) return ((PerceptualHash) o).sideLength == this.sideLength;
		else return false; // Note above if sideLength is the same, so must be the coefficients.
	}

	@Override
	public boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode) {
		if (!this.canCompare(hash1, hash2)) {
			throw new IllegalArgumentException("Algorithm " + hash1.getAlgName() + " and algorithm "
					+ hash2.getAlgName() + " are not comparable under algorithm " + this.algName() + ".");
		}

		// Doubles are represented exactly for a very large number of bits, so this is
		// okay.
		double dist = this.distance(hash1, hash2);
		switch (mode) {
		case SLOPPY:
			return dist < (8 / (double) 64) * this.getHashLength();
		case NORMAL:
			return dist < (5 / (double) 64) * this.getHashLength();
		case STRICT:
			return dist < (2 / (double) 64) * this.getHashLength();
		case EXACT:
			return dist == 0;
		default:
			throw new IllegalArgumentException("Invalid MatchMode: " + mode);
		}
	}

	@Override
	public ImageHash hash(IImage<?> img) {
		// Algorithm summary can be found here:
		// http://hackerfactor.com/blog/index.php%3F/archives/432-Looks-Like-It.html
		// And also check out phash.org

		double[] transformedTrimmedDCT = HalfDCTII.halfDCTII(resize(img, this.sideLength), this.sideLength,
				this.DCTCoefficients);

		// Now we put the bits of the hash into a long[], and make an ImageHash object
		// out of it.
		return new ImageHash(this, constructHash(transformedTrimmedDCT),
				img instanceof SourcedImage ? ((SourcedImage) img).getSource() : null);
	}

	// Resize the image, and convert it to a 2d array of doubles.
	private static byte[] resize(IImage<?> img, int size) {
		return img.resizeBilinear(size, size).toGreyscale().getPixels();
	}

	private byte[] constructHash(double[] transformedTrimmedDCT) {

		// First, calculate the mean value of the whole thing. Doing so in batches
		// allows for much smaller average loss in precision.
		double meanValue = avg(transformedTrimmedDCT);

		// Now set the bits of the hash.
		byte[] hashValues = new byte[((transformedTrimmedDCT.length) + 7) / 8];

		int longPos = 0, currentLong = -1;
		for (int i = 0; i < transformedTrimmedDCT.length; i++) {
				if (longPos % 8 == 0) {
					// -1 will immediately be incremented to 0, and it will spill over into new
					// longs when necessary.
					currentLong++;
					longPos = 0;
				}

				// Set the current bit of the hash
				hashValues[currentLong] <<= 1;
				hashValues[currentLong] |= transformedTrimmedDCT[i] > meanValue ? 1 : 0;
				longPos++;
		}

		// Slide the last entry of the hash in to the left if necessary.
		hashValues[currentLong] <<= 8 - longPos;

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
