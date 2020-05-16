package hash.implementations;

import java.awt.image.BufferedImage;

import hash.ComparisonType;
import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.PixelUtils;
import image.implementations.GreyscaleImage;

public class DifferenceHash implements IHashAlgorithm {

	public DifferenceHash() {
		this(8);
	}

	public DifferenceHash(int sideLength) throws ArithmeticException {
		try {
			PixelUtils.safeSquare(sideLength);
		} catch (ArithmeticException e) {
			throw new IllegalArgumentException(e);
		}
		this.sideLength = sideLength;
	}

	private int sideLength;

	@Override
	public String getHashName() {
		return "dHash";
	}

	@Override
	public int getHashLength() {
		return sideLength * sideLength;
	}

	@Override
	public ComparisonType getComparisonType() {
		return ComparisonType.HAMMING;
	}

	@Override
	public String serialize() {
		return "" + this.sideLength;
	}

	@Override
	public IHashAlgorithm deserialize(String serialized) throws IllegalArgumentException {
		try {
			return new DifferenceHash(Integer.parseInt(serialized.trim()));
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
		// This size seems odd, but we're averaging the pixels next to each other
		// horizontally, and end up with an sideLength x sideLength length hash.
		int rowLength = this.sideLength + 1;
		img = img.resizeBilinear(rowLength, this.sideLength);
		byte[] thumbnail = img.toGreyscale().getPixels();

		int thumbnailPixelNum = rowLength * this.sideLength;

		// Also worth noting is that resizing before greyscaling is more efficient. You
		// wouldn't think that it would work this way, but for some reason it does.

		int hashLongLength = (this.sideLength * this.sideLength + 63) / 64;
		long[] hash = new long[hashLongLength];
		int finishedIndex = -1, thumbnailAccumulator = 0;
		int longPos = 0;

		// Set each bit of the hash depending on value adjacent
		for (; thumbnailAccumulator < thumbnailPixelNum; thumbnailAccumulator++) {

			if (thumbnailAccumulator % rowLength == this.sideLength) {
				// If there's a beginning of a next row, skip this one.
				continue;
			}

			if (longPos % 64 == 0) {
				// fineshedIndex's -1 will immediately be incremented to 0, and it will spill
				// over into new longs when necessary.
				finishedIndex++;
				longPos = 0;
			}

			// Set the current bit of the hash
			hash[finishedIndex] <<= 1;
			hash[finishedIndex] |= (thumbnail[thumbnailAccumulator] & 0xff) < (thumbnail[thumbnailAccumulator + 1]
					& 0xff) ? 1 : 0;
			longPos++;
		}

		// Shift in
		hash[finishedIndex] <<= 64 - longPos;

		/*
		 * // Reverse all bits, because of the pushing back to build the hash. This is
		 * not // actually necessary, but makes the visual representation look better.
		 * for (finishedIndex = 0; finishedIndex < hash.length; finishedIndex++) {
		 * hash[finishedIndex] = Long.reverse(hash[finishedIndex]); }
		 */
		return new ImageHash(this, hash, this.findSource(img));
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		return hash(new GreyscaleImage(img));
	}

}
