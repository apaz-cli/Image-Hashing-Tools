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

public class AverageHash implements IHashAlgorithm {

	static {
		AlgLoader.register(new AverageHash());
	}

	public AverageHash() {
		this.sideLength = 8;
	}

	public AverageHash(int sideLength) {
		try {
			PixelUtils.safeSquare(sideLength);
		} catch (ArithmeticException e) {
			throw new IllegalArgumentException(e);
		}
		this.sideLength = sideLength;
	}

	private int sideLength;

	@Override
	public String algName() {
		return "aHash";
	}

	@Override
	public int getHashLength() { return this.sideLength * this.sideLength; }

	@Override
	public ComparisonType getComparisonType() { return ComparisonType.HAMMING; }

	@Override
	public String toArguments() {
		return "" + this.sideLength;
	}

	@Override
	public IHashAlgorithm fromArguments(String serialized) throws IllegalArgumentException {
		try {
			return new AverageHash(Integer.parseInt(serialized.trim()));
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
		if (o instanceof AverageHash) return ((AverageHash) o).sideLength == this.sideLength;
		else return false;
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
		// Resizing before converting to greyscale is 1.5 to 2x faster.
		// I was really confused about that when I benchmarked it, but it's true.

		byte[] thumbnail = img.resizeBilinear(this.sideLength, this.sideLength).toGreyscale().getPixels();

		// Take an average. Note that there's no risk of overflow with a double.
		// b/c Double.MAX_VALUE > Integer.MAX_VALUE * 255
		double average = 0;
		for (byte b : thumbnail) {
			average += b & 0xff;
		}
		average /= thumbnail.length;

		int thumbnailOffset = 0;
		byte[] hash = new byte[(this.sideLength * this.sideLength + 7) / 8];

		// Set bits into hash
		int idx = 0;
		while (thumbnailOffset < thumbnail.length) {
			hash[idx] |= (thumbnail[thumbnailOffset++] & 0xff) < average ? 0x1 : 0x0;
			for (int i = 1; i < 8; i++) {
				// Doing it like this, while it does slow things down somewhat, ensures that the
				// last byte is properly shifted all the way to the left.
				hash[idx] <<= 1;
				if (thumbnailOffset < thumbnail.length) {
					hash[idx] |= (thumbnail[thumbnailOffset++] & 0xff) < average ? 0x1 : 0x0;
				}
			}
			idx++;
		}

		return new ImageHash(this, hash, img instanceof SourcedImage ? ((SourcedImage) img).getSource() : null);
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		return hash(new GreyscaleImage(img));
	}

}
