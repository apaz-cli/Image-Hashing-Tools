package hash.implementations;

import java.awt.image.BufferedImage;

import hash.AlgLoader;
import hash.ComparisonType;
import hash.HashUtils;
import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.PixelUtils;
import image.implementations.GreyscaleImage;
import image.implementations.SourcedImage;

public class DifferenceHash implements IHashAlgorithm {

	static {
		AlgLoader.register(new DifferenceHash());
	}

	public DifferenceHash() { this.sideLength = 16; }

	public DifferenceHash(int sideLength) throws ArithmeticException {
		if (sideLength < 1) throw new IllegalArgumentException("Side length is too small.");
		try {
			PixelUtils.safeSquare(sideLength);
		} catch (ArithmeticException e) {
			throw new IllegalArgumentException(e);
		}
		this.sideLength = sideLength;
	}

	public DifferenceHash(int sideLength, MatchMode mode) { this.setDefaultMatchMode(mode); }

	private int sideLength;

	private MatchMode defaultMode = MatchMode.NORMAL;

	@Override
	public void setDefaultMatchMode(MatchMode mode) { if (mode != null) this.defaultMode = mode; }

	@Override
	public MatchMode getDefaultMatchMode() { return defaultMode; }

	@Override
	public String algName() { return "dHash"; }

	@Override
	public int getHashLength() { return sideLength * sideLength; }

	@Override
	public ComparisonType getComparisonType() { return ComparisonType.HAMMING; }

	@Override
	public String toArguments() { return "" + this.sideLength; }

	@Override
	public IHashAlgorithm fromArguments(String serialized) throws IllegalArgumentException {
		try {
			return new DifferenceHash(Integer.parseInt(serialized.trim()));
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
		if (o instanceof DifferenceHash) return ((DifferenceHash) o).sideLength == this.sideLength;
		else return false;
	}

	@Override
	public boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode) {
		if (!this.canCompare(hash1, hash2)) {
			throw new IllegalArgumentException("Algorithm " + hash1.getAlgName() + " and algorithm "
					+ hash2.getAlgName() + " are not comparable under algorithm " + this.algName() + ".");
		}
		if (mode == null) mode = defaultMode;

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
		// This size seems odd, but we're averaging the pixels next to each other
		// horizontally, and end up with an sideLength x sideLength length hash.
		byte[] thumbnail = img.resizeBilinear(this.sideLength, this.sideLength).toGreyscale().getPixels();

		int numHashBits = thumbnail.length - this.sideLength;
		byte[] hash = new byte[(numHashBits + 7) / 8];

		int offset = 0, hashOffset = 0, byteIndex = 0;
		while ((offset += 1) < thumbnail.length) {
			if (offset % this.sideLength == 0) continue;
			else {
				int bit = (thumbnail[offset] & 0xff) > (thumbnail[offset - 1] & 0xff) ? 0x1 : 0x0;
				hash[hashOffset] <<= 1;
				hash[hashOffset] |= bit;

				// Move onto the next hash index if we must.
				byteIndex++;
				if (byteIndex == 8) {
					hashOffset++;
					byteIndex = 0;
				}
			}
		}

		// Shift in the last byte of the array, since that didn't get done above
		hash[hash.length - 1] <<= (8 - (this.getHashLength() % 8));

		return new ImageHash(this, hash, img instanceof SourcedImage ? ((SourcedImage) img).getSource() : null);
	}

	@Override
	public ImageHash hash(BufferedImage img) { return hash(new GreyscaleImage(img)); }

}
