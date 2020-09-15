package hash;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import hashstore.vptree.MetricComparable;
import image.PixelUtils;
import image.implementations.SourcedImage;
import utils.ImageUtils;

public class ImageHash implements Serializable, MetricComparable<ImageHash> {

	/********************/
	/* Member Variables */
	/********************/

	// For Java serialization
	private static final long serialVersionUID = -1248134817737622671L;

	// The hash itself
	private final byte[] bits;

	// @nof
	// This field is stored as one string for memory reasons. It should be laid out with:
	// hashName,hashLength,comparisonType
	// With all of the information coming from the IHashAlgorithm that created it. It's stored this way because 
	// Strings are interned, meaning that only one instance of that string actually exists, and each ImageHash
	// only needs to hold a reference to it.
	// @dof
	private final IHashAlgorithm creator;

	// Where the Image came from, if it was a SourcedImage. Otherwise null. Note
	// that null appends as "null" with StringBuilder in toString(), which
	// effectively serializes hashes.
	private String source = null;

	// Assume that the creator has been implemented correctly, and that the
	// hashInformation is laid out properly. But, for String constructors, we'll
	// check.

	/****************/
	/* Constructors */
	/****************/

	public ImageHash(IHashAlgorithm creator, byte[] bits) throws IllegalArgumentException {
		PixelUtils.assertNotNull(new String[] { "creator", "bits" }, creator, bits);
		// + (bits.length % 8) is to resolve the buffer cast issue
		this.bits = Arrays.copyOf(bits, bits.length + (bits.length % 8));
		this.creator = creator;
		// Source remains null
	}

	public ImageHash(IHashAlgorithm creator, byte[] bits, String source) throws IllegalArgumentException {
		PixelUtils.assertNotNull(new String[] { "creator", "bits" }, creator, bits);
		// + (bits.length % 8) is to resolve the buffer cast issue
		this.bits = Arrays.copyOf(bits, bits.length + (bits.length % 8));
		this.creator = creator;
		this.source = source;
	}

	// Copy constructor
	public ImageHash(ImageHash h) {
		this.bits = h.bits;
		this.creator = h.creator;
		this.source = h.source;
	}

	/*****************/
	/* Serialization */
	/*****************/
	@Override
	public String toString() {
		// @nof
		return new StringBuilder()
				.append(this.hexHash()).append(",")
				.append(creator.algName()).append(",")
				.append(creator.toArguments()).append(',')
				.append(this.source).toString();
		// @dof
	}

	public static ImageHash fromString(String imageHash) throws IllegalArgumentException, ClassNotFoundException {
		// Parse for arguments
		// hexBits,algName,hashLength,comparisonType,source

		String bits = null, algName = null, algArgs = null, source = null;
		String[] split = imageHash.split(",");
		// bits,algName,arg║arg║arg...,source

		if (split.length < 4) {
			throw new IllegalArgumentException(
					"Failed to parse an ImageHash from the given string. Expected a String in the form: "
							+ "bits,algName,args,source but instead got " + imageHash + " which only has "
							+ split.length + " parts.");
		} else if (split.length == 4) {
			bits = split[0];
			algName = split[1];
			algArgs = split[2];
			source = split[3];
			// If source is missing, just leave it null.
		} else if (split.length > 4) {
			bits = split[0];
			algName = split[1];
			algArgs = split[2];
			// Join the trailing pieces of the split onto the source. This fixes urls having
			// commas in them.
			source = split[3];
			for (int i = 4; i < split.length; i++) {
				source += ',' + split[i];
			}
		}

		int hexCharCount = bits.length();
		byte[] bytes = new byte[(hexCharCount / 2) + (hexCharCount % 2)];

		int currentByte = 0; // For packing
		boolean firstNibble = true;
		for (char hexChar : (bits = bits.toUpperCase()).toCharArray()) {
			// Valid Range: 48-57, 65-70
			byte nibble = hexCharToByte(hexChar);
			if (firstNibble) {
				bytes[currentByte] = (byte) (nibble << 4);
				firstNibble = false;
			} else {
				bytes[currentByte] = (byte) (bytes[currentByte] | nibble);
				currentByte++;
				firstNibble = true;
			}
		}

		// Get creator, throws ClassNotFound if can't find. (algName/algArgs are invalid
		// or algorithm hasn't been loaded)
		IHashAlgorithm creator = AlgLoader.loadAlgorithm(algName, algArgs);

		// Resize the bytes we parsed into a buffer of the size the creator is
		// expecting.
		int len = creator.getHashLength();
		len = len + (len % 8);
		len /= 8;

		if (len < bytes.length) {
			throw new IllegalArgumentException("Did not read enough bits to create a hash. Expected " + len
					+ " bytes of data, corresponding to " + len * 2 + " characters. Got : " + bits.toUpperCase()
					+ ", only " + bits.length() + " characters.");
		}
		bytes = Arrays.copyOfRange(bytes, 0, len);

		// There's a similar approach to error checking taken elsewhere. We throw
		// IllegalArgumentException bit packing above if the bits are invalid, and we
		// just trust that the user knows what to do with the Source.

		return new ImageHash(creator, bytes, source);
	}

	private static byte hexCharToByte(char hexChar) throws IllegalArgumentException {
		int hexInt = hexChar - 48;
		// Shift to 0-9, 17-22
		if (hexInt < 0x0) {
			throw new IllegalArgumentException("hexBits contain non-hex characters.");
		} else if (hexInt > 0x9) {
			// Collapse lower range so 0-9, 10-15 for hex integer
			hexInt -= 7;
			if (hexInt > 0xF) { throw new IllegalArgumentException("Serialized hash contains non-hex characters."); }
		}
		return (byte) hexInt;
	}

	/***********/
	/* Getters */
	/***********/

	public BitSet getBitSet() { return BitSet.valueOf(this.bits); }

	public byte[] getBits() { return this.bits; }

	public IHashAlgorithm getAlgorithm() { return this.creator; }

	// These should never throw exceptions. If they do, an IHashAlgorithm was
	// implemented incorrectly.

	public String getAlgName() { return this.creator.algName(); }

	public int getLength() { return this.creator.getHashLength(); }

	public String getSource() { return this.source; }

	public SourcedImage loadFromSource() throws IOException {
		if (this.source == null || this.source.equals("null")) throw new IOException("This image has no source.");

		boolean isURL = ImageUtils.validURL(this.source);

		if (isURL) {
			try {
				return new SourcedImage(new URL(this.source));
			} catch (MalformedURLException e) { // We already tested this, it can be ignored.
			}
		} else {
			return new SourcedImage(new File(this.source));
		}

		throw new IOException("Was not able to load " + (isURL ? "url" : "file") + ": " + this.source);
	}

	@Override
	public double distance(ImageHash hash) throws IllegalArgumentException { return this.creator.distance(this, hash); }

	public int[] bitsToIntArray() {
		int[] ints = new int[(this.getLength() + 31) / 32];
		ByteBuffer.wrap(this.bits).asIntBuffer().get(ints);
		return ints;
	}

	public long[] bitsToLongArray() {
		long[] longs = new long[(this.getLength() + 63) / 64];
		ByteBuffer.wrap(this.bits).asLongBuffer().get(longs);
		return longs;
	}

	public float[] bitsToFloatArray() {
		float[] floats = new float[(this.getLength() + 31) / 32];
		ByteBuffer.wrap(this.bits).asFloatBuffer().get(floats);
		return floats;
	}

	public double[] bitsToDoubleArray() {
		double[] doubles = new double[(this.getLength() + 63) / 64];
		ByteBuffer.wrap(this.bits).asDoubleBuffer().get(doubles);
		return doubles;
	}

	// Convenience for toString() hex conversion
	protected final static char[] intToHexChar = "0123456789ABCDEF".toCharArray();

	// Returns the contents of this hash's bits in hexadecimal.
	private String hexHash() {
		char[] encodedChars = new char[this.bits.length * 2];
		for (int i = 0; i < this.bits.length; i++) {
			int b = this.bits[i];
			char c1 = intToHexChar[(b & 0xf0) >> 4];
			char c2 = intToHexChar[(b & 0x0f)];
			encodedChars[i * 2] = c1;
			encodedChars[i * 2 + 1] = c2;
		}
		return new String(encodedChars);
	}

	@Override
	public boolean equals(Object h) {
		if (h == null) return false;
		if (!(h instanceof ImageHash)) return false;
		ImageHash o = (ImageHash) h;
		String thatSauce = o.source == null ? "null" : o.source;
		String thisSauce = this.source == null ? "null" : this.source;
		return Arrays.equals(this.bits, o.bits) && this.creator.canCompare(this, o) && thisSauce.equals(thatSauce);
	}

	// Deep equals for everything but the source
	public boolean equalsIgnoreSource(Object h) {
		if (h == null) return false;
		if (!(h instanceof ImageHash)) return false;
		ImageHash o = (ImageHash) h;
		return Arrays.equals(this.bits, o.bits) && this.creator.canCompare(this, o);
	}

	// throws when hashes are uncomparable
	public boolean matches(ImageHash h) throws IllegalArgumentException { return this.creator.matches(this, h); }

	// throws when hashes are uncomparable
	public boolean matches(ImageHash h, MatchMode mode) throws IllegalArgumentException {
		return this.creator.matches(this, h, mode);
	}

}
