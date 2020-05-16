package hash.implementations;

import java.awt.image.BufferedImage;

import hash.ComparisonType;
import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.implementations.GreyscaleImage;

public class AverageHash implements IHashAlgorithm {

	public AverageHash() {
		this.sideLength = 8;
	}

	public AverageHash(int sideLength) {
		this.sideLength = sideLength;
	}

	int sideLength;

	@Override
	public String getHashName() {
		return "aHash";
	}

	@Override
	public int getHashLength() {
		return 64;
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
			return new AverageHash(Integer.parseInt(serialized.trim()));
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

		// Doubles are represented exactly for a very large number of bits, so this is okay.
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
		// Resizing before converting to greyscale is 1.5 to 2x faster.
		// I was really confused about that when I benchmarked it, but it's true.
		img = img.resizeBilinear(this.sideLength, this.sideLength);

		// toGreyscale does not create an extra object if typeof image is
		// GreyscaleImage. It just returns itself, so this has no overhead if img is not
		// already a GreyscaleImage.
		byte[] thumbnail = img.toGreyscale().getPixels();

		// Take an average. Note that there's no risk of overflow with a double.
		// b/c Double.MAX_VALUE > Integer.MAX_VALUE * 255
		double average = 0;
		for (byte b : thumbnail) {
			average += b & 0xff;
		}
		average /= thumbnail.length;

		// Set bits into hash
		int thumbnailOffset = 0;
		long[] hash = new long[(this.sideLength * this.sideLength + 63) / 64];
		for (int idx = 0; idx < hash.length; idx++) {
			hash[idx] |= (thumbnail[thumbnailOffset++] & 0xff) < average ? 0x1 : 0x0;
			for (int i = 1; i < 64; i++) {
				if (thumbnailOffset < thumbnail.length) {
					hash[idx] |= (thumbnail[thumbnailOffset++] & 0xff) < average ? 0x1 : 0x0;
				}
				hash[idx] <<= 1;
			}
		}

		// Reverse all bits, because of the pushing back to build the hash. This is not
		// actually necessary, but makes the visual representation look better.
		for (int i = 0; i < hash.length; i++) {
			hash[i] = Long.reverse(hash[i]);
		}

		return new ImageHash(this, hash, this.findSource(img));
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		return hash(new GreyscaleImage(img));
	}

}
