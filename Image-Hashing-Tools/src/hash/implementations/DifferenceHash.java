package hash.implementations;

import java.awt.image.BufferedImage;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.implementations.GreyscaleImage;

public class DifferenceHash implements IHashAlgorithm {

	public DifferenceHash() {
		this(8);
	}

	public DifferenceHash(int sideLength) {
		this.sideLength = sideLength;
		this.hashLength = sideLength * sideLength;
	}

	private int sideLength;
	private int hashLength;

	@Override
	public String getHashName() {
		return "dHash";
	}

	@Override
	public int getHashLength() {
		return this.hashLength;
	}

	@Override
	public boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode) {
		// This assertion assures that the hashes are actually comparable.
		if (!hash1.getType().equals(this.getHashName()) || hash1.getLength() != hash2.getLength()) {
			throw new IllegalArgumentException(
					"These hashes are not comparable. The hashes being compared must be of the same type and length.");
		}

		// No need to assert comparable, Hamming distance method does this.
		if (mode == MatchMode.SLOPPY) {
			return hash1.hammingDistance(hash2) < 8;
		} else if (mode == MatchMode.NORMAL) {
			return hash1.hammingDistance(hash2) < 5;
		} else if (mode == MatchMode.STRICT) {
			return hash1.hammingDistance(hash2) < 2;
		} else if (mode == MatchMode.EXACT) {
			return hash1.hammingDistance(hash2) == 0;
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

		int hashLongLength = (this.hashLength + 63) / 64;
		long[] finishedHash = new long[hashLongLength];
		int finishedIndex = -1, thumbnailAccumulator = 0;
		int longPos = 0;

		// Set each bit of the hash depending on value adjacent
		for (; thumbnailAccumulator < thumbnailPixelNum; thumbnailAccumulator++) {

			if (thumbnailAccumulator % rowLength == this.sideLength) {
				// If there's a beginning of a next row, skip this one.
				continue;
			}

			if (longPos % 64 == 0) {
				// -1 will immediately be incremented to 0, and it will spill over into new
				// longs when necessary.
				finishedIndex++;
				longPos = 0;
			}

			// Set the current bit of the hash
			finishedHash[finishedIndex] <<= 1;
			finishedHash[finishedIndex] |= (thumbnail[thumbnailAccumulator]
					& 0xff) < (thumbnail[thumbnailAccumulator + 1] & 0xff) ? 1 : 0;
			longPos++;
		}

		// Shift in
		finishedHash[finishedIndex] <<= 64 - longPos;

		/*
		 * // Reverse in place for (int i = 0; i < finishedHash.length / 2; i++) { long
		 * temp = finishedHash[i]; finishedHash[i] =
		 * Long.reverse(finishedHash[finishedHash.length - i - 1]);
		 * finishedHash[finishedHash.length - i - 1] = Long.reverse(temp); }
		 */

		return new ImageHash(this.getHashName(), finishedHash, this.hashLength);
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		return hash(new GreyscaleImage(img));
	}

}
