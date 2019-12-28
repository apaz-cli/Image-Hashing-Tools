package hash.implementations;

import java.awt.image.BufferedImage;
import java.util.BitSet;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.implementations.GreyscaleImage;

public class AverageHash implements IHashAlgorithm {

	@Override
	public String getHashName() {
		return "aHash";
	}

	@Override
	public int getHashLength() {
		return 64;
	}
	
	@Override
	public boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode) {

		// This assertion assures that
		if (!hash1.getType().equals(this.getHashName())) {
			throw new IllegalArgumentException(
					"Another method must be used to compare nonstandard variations of this hash. "
				  + "Also, make sure you are comparing to hashes of the same type.");
		}

		// No need to assert comparable, Hamming distance method does this.
		if (mode == MatchMode.SLOPPY) {
			return hash1.hammingDistance(hash2) < 8;
		} else if (mode == MatchMode.NORMAL) {
			return hash1.hammingDistance(hash2) < 5;
		} else if (mode == MatchMode.STRICT) {
			return hash1.hammingDistance(hash2) < 2;
		}
		// MatchMode.EXACT
		return hash1.hammingDistance(hash2) == 0;
	}

	@Override
	public ImageHash hash(IImage<?> img) {
		// Resizing before converting to greyscale is 1.5 to 2x faster.
		// I was really confused about that when I benchmarked it, but it's true.
		img = img.resizeBilinear(8, 8);

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

		// Compute average hash using average
		BitSet hash = new BitSet(64);
		for (int i = 0; i < 64; i++) {
			if ((thumbnail[i] & 0xff) > average) {
				hash.set(i);
			}
		}
		return new ImageHash(this.getHashName(), hash, 64);
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		return hash(new GreyscaleImage(img));
	}

}
