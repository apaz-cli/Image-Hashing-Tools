package hash.implementations;

import java.awt.image.BufferedImage;
import java.util.BitSet;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.implementations.GreyscaleImage;

public class DifferenceHash implements IHashAlgorithm {

	public DifferenceHash() {
	}

	@Override
	public String getHashName() {
		return "dHash";
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
		// This size seems odd, but we're averaging the pixels next to each other
		// horizontally, and end up with an 8x8 hash.
		img = img.resizeBilinear(9, 8);
		byte[] thumbnail = img.toGreyscale().getPixels();

		// Also worth noting is that resizing first is more efficient. You wouldn't
		// think that it would work this way, but for some reason it does.

		// Set each bit of the hash depending on value adjacent
		BitSet bs = new BitSet(64);

		// For efficiency, hardcode the inner loop.
		int fullAccumulator = 0, hashRowAccumulator = 0;
		int pixel1, pixel2, pixel3, pixel4, pixel5, pixel6, pixel7, pixel8, pixel9;
		for (; fullAccumulator < thumbnail.length;) {
			// For each row,
			pixel1 = thumbnail[fullAccumulator++] & 0xff;
			pixel2 = thumbnail[fullAccumulator++] & 0xff;
			pixel3 = thumbnail[fullAccumulator++] & 0xff;
			pixel4 = thumbnail[fullAccumulator++] & 0xff;
			pixel5 = thumbnail[fullAccumulator++] & 0xff;
			pixel6 = thumbnail[fullAccumulator++] & 0xff;
			pixel7 = thumbnail[fullAccumulator++] & 0xff;
			pixel8 = thumbnail[fullAccumulator++] & 0xff;
			pixel9 = thumbnail[fullAccumulator++] & 0xff;

			if (pixel1 < pixel2) {
				bs.set(hashRowAccumulator);
			}
			hashRowAccumulator++;
			if (pixel2 < pixel3) {
				bs.set(hashRowAccumulator);
			}
			hashRowAccumulator++;
			if (pixel3 < pixel4) {
				bs.set(hashRowAccumulator);
			}
			hashRowAccumulator++;
			if (pixel4 < pixel5) {
				bs.set(hashRowAccumulator);
			}
			hashRowAccumulator++;
			if (pixel5 < pixel6) {
				bs.set(hashRowAccumulator);
			}
			hashRowAccumulator++;
			if (pixel6 < pixel7) {
				bs.set(hashRowAccumulator);
			}
			hashRowAccumulator++;
			if (pixel7 < pixel8) {
				bs.set(hashRowAccumulator);
			}
			hashRowAccumulator++;
			if (pixel8 < pixel9) {
				bs.set(hashRowAccumulator);
			}
			hashRowAccumulator++;
		}

		return new ImageHash(this.getHashName(), bs, 64);
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		return hash(new GreyscaleImage(img));
	}

}
