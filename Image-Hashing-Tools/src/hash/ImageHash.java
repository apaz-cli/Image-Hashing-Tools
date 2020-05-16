package hash;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

import hashstore.MetricComparable;
import image.PixelUtils;

public class ImageHash implements Serializable, MetricComparable<ImageHash> {

	/********************/
	/* Member Variables */
	/********************/

	// For Java serialization
	private static final long serialVersionUID = -1248134817737622671L;
	// Convenience for toString() hex conversion
	final protected static char[] intToHexChar = "0123456789ABCDEF".toCharArray();

	// The hash itself
	private final long[] bits;

	// @nof
	// This field is stored as one string for memory reasons. It should be laid out with:
	// hashName,hashLength,comparisonType
	// With all of the information coming from the IHashAlgorithm that created it. It's stored this way because 
	// Strings are interned, meaning that only one instance of that string actually exists, and each ImageHash
	// only needs to hold a reference to it.
	// @dof
	private final String hashInformation;

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

	public ImageHash(IHashAlgorithm creator, long[] bits) throws IllegalArgumentException {
		PixelUtils.assertNotNull(new String[] { "creator", "bits" }, creator, bits);
		this.bits = Arrays.copyOf(bits, bits.length);
		this.hashInformation = creator.getHashInformation(); // Source remains null
	}

	public ImageHash(IHashAlgorithm creator, long[] bits, String source) throws IllegalArgumentException {
		PixelUtils.assertNotNull(new String[] { "creator", "bits" }, creator, bits);
		this.bits = Arrays.copyOf(bits, bits.length);
		this.hashInformation = creator.getHashInformation();
		this.source = sanitizeFileOrURL(source);
	}

	public ImageHash(String hashInformation, long[] bits) throws IllegalArgumentException {
		PixelUtils.assertNotNull(new String[] { "hashInformation", "bits" }, hashInformation, bits);
		this.bits = Arrays.copyOf(bits, bits.length);
		this.hashInformation = checkHashInformation(hashInformation);
	}

	public ImageHash(String hashInformation, long[] bits, String source) throws IllegalArgumentException {
		PixelUtils.assertNotNull(new String[] { "hashInformation", "bits" }, hashInformation, bits);
		this.bits = Arrays.copyOf(bits, bits.length);
		this.hashInformation = checkHashInformation(hashInformation);
		this.source = sanitizeFileOrURL(source);
	}

	/*****************/
	/* Sanity Checks */
	/*****************/

	private static void checkAlgName(String type) throws IllegalArgumentException {
		for (char c : type.toCharArray()) {
			if (c == '|' || c == ',' || c == '\"') {
				throw new IllegalArgumentException(
						"The hashName field of the hashInformation may not contain the characters comma, pipe, or double quote (,|\").");
			}
		}
	}

	private static void checkHashLength(String hashLength) {
		try {
			Integer.parseInt(hashLength);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					"Could not parse an int for the hash's length from the hashInformation provided.");
		}

	}

	private static void checkComparisonType(String comparisonType) {
		try {
			ComparisonType.valueOf(comparisonType);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not deserialize a valid ComparisonType from the hashInformation provided.");
		}
	}

	private static String checkHashInformation(String hashInformation) throws IllegalArgumentException {
		String[] spt = hashInformation.split(",");
		if (spt.length != 3) throw new IllegalArgumentException(
				"hashInformation does not have the correct number of fields. Expected algName,hashLength,comparisonType, but got: "
						+ hashInformation);
		checkAlgName(spt[0]);
		checkHashLength(spt[1]);
		checkComparisonType(spt[2]);
		return hashInformation;
	}

	// Encode any backslashes or pipes with their url escape equivalents
	private String sanitizeFileOrURL(String source) {
		return source == null ? null : source.replace("\\", "%5C").replace("|", "%7C");
	}

	/***********/
	/* Getters */
	/***********/

	public BitSet getBitSet() {
		return BitSet.valueOf(this.bits);
	}

	public long[] getBitArray() {
		return this.bits;
	}

	public String getHashInformation() {
		return this.hashInformation;
	}

	// These should never throw exceptions. If they do, an IHashAlgorithm was
	// implemented incorrectly.

	public String getAlgName() {
		return this.hashInformation.split(",")[0];
	}

	public int getHashLength() {
		return Integer.parseInt(this.hashInformation.split(",")[1]);
	}

	public ComparisonType getComparisonType() {
		return ComparisonType.valueOf(this.hashInformation.split(",")[2]);
	}

	public String getSource() {
		return this.source;
	}

	/*****************************/
	/* Strings and Serialization */
	/*****************************/

	public static ImageHash fromString(String imageHash) throws NumberFormatException, IllegalArgumentException {
		// Parse for arguments
		// hexBits,algName,hashLength,comparisonType,source
		String hexBits = null, hashName = null, hashLength = null, comparisonType = null, source = null;
		String[] split = imageHash.split(",");

		if (split.length < 4) {
			throw new IllegalArgumentException(
					"Failed to parse an ImageHash from the given string. Expected a String in the form: "
							+ "hashName,hashLength,comparisonType,hexBits,source where source "
							+ "could possibly be \"null\", missing, or a url containing commas. Instead got: "
							+ imageHash);
		}
		if (split.length == 4) {
			hexBits = split[0];
			hashName = split[1];
			hashLength = split[2];
			comparisonType = split[3];
			// If source is missing, just leave it null.
		}
		if (split.length == 5) {
			hexBits = split[0];
			hashName = split[1];
			hashLength = split[2];
			comparisonType = split[3];
			source = split[4];
		} else if (split.length > 5) {
			hexBits = split[0];
			hashName = split[1];
			hashLength = split[2];
			comparisonType = split[3];
			// If source has a comma in it, just join the trailing pieces of the split.
			// We'll sanity check this later.
			source = "";
			for (int i = 4; i < split.length; i++) {
				source += split[i];
			}
		}

		long[] longs = new long[(Integer.valueOf(hashLength) + 63) / 64];

		int currentLong = 0, nibbleOffset = 0;
		int c;
		for (char hexChar : (hexBits = hexBits.toUpperCase()).toCharArray()) {
			c = hexChar - 48;
			if (c < 0x0) {
				throw new IllegalArgumentException("hexBits contain non-hex characters.");
			} else if (c > 0x9) {
				c -= 7;
				if (c < 0xA || c > 0xF) {
					throw new IllegalArgumentException("hexBits contain non-hex characters.");
				}
			}

			if (nibbleOffset < 16) {
				nibbleOffset++;
				longs[currentLong] |= (0xF & c);
				if (nibbleOffset != 16) {
					longs[currentLong] <<= 0x4;
				}
			} else {
				currentLong++;
				longs[currentLong] |= (0xF & c);
				longs[currentLong] <<= 0x4;
				nibbleOffset = 1;
			}
		}

		// Let the constructor do the sanity checks.
		String hashInformation = new StringBuilder(hashName).append(",").append(hashLength).append(",")
				.append(comparisonType).toString();
		return new ImageHash(hashInformation, longs, source);
	}

	@Override
	public String toString() {
		// @nof
		return new StringBuilder()
				.append(this.hexHash()).append(",")
				.append(this.hashInformation).append(",")
				.append(this.source).toString();
		// @dof
	}

	public double[] getBitArrayAsDouble() {
		double[] dbits = new double[this.bits.length];
		for (int i = 0; i < this.bits.length; i++) {
			dbits[i] = java.lang.Double.longBitsToDouble(this.bits[i]);
		}
		return dbits;
	}

	public float[] getBitArrayAsFloat() {
		int[] ibits = this.getBitArrayAsInt();
		float[] fbits = new float[ibits.length];
		for (int i = 0; i < ibits.length; i++) {
			fbits[i] = java.lang.Float.intBitsToFloat(ibits[i]);
		}
		return fbits;
	}

	public int[] getBitArrayAsInt() {
		int[] ibits = new int[this.bits.length * 2];
		int currentInt;
		for (int i = 0; i < this.bits.length; i++) {
			currentInt = i * 2;
			ibits[currentInt] = (int) this.bits[i];
			ibits[currentInt + 1] = (int) (this.bits[i] >> 32);
		}
		return ((this.getHashLength() / 32) % 2 == 0) ? ibits : Arrays.copyOf(ibits, (this.bits.length * 2) - 1);
	}

	public byte[] getBitArrayAsByte() {
		byte[] bbits = new byte[this.bits.length * 8];
		int currentByte;
		for (int i = 0; i < this.bits.length; i++) {
			currentByte = i * 8;
			long workingLong = this.bits[i];

			bbits[currentByte] = (byte) workingLong;
			for (int j = 1; j < 8; j++) {
				workingLong <<= 8;
				bbits[currentByte + j] = (byte) workingLong;
			}
		}
		return bbits;
	}

	public boolean getBit(int bitIndex) throws ArrayIndexOutOfBoundsException {
		// https://docs.oracle.com/javase/7/docs/api/java/utils/BitSet.html#toLongArray%28%29
		return ((this.bits[bitIndex / 64] & (1L << (bitIndex % 64))) != 0);
	}

	@Override
	public double distance(ImageHash hash) throws IllegalArgumentException {
		assertComparable(hash);

		ComparisonType thisType = this.getComparisonType();
		switch (thisType) {
		case HAMMING:
			return this.hammingDistance(hash);
		case EUCLIDEANF32:
			return this.euclideanF32Distance(hash);
		case EUCLIDEANF64:
			return this.euclideanF64Distance(hash);
		default:
			throw new UnsupportedOperationException("ComparisonType \"" + thisType + "\" not supported.");
		}
	}

	private int hammingDistance(ImageHash hash) {
		long[] other = hash.getBitArray();
		int distance = 0;
		long b;
		for (int idx = 0; idx < this.bits.length; idx++) {
			b = this.bits[idx] ^ other[idx];
			b -= (b >> 1) & 0x5555555555555555L;
			b = (b & 0x3333333333333333L) + ((b >> 2) & 0x3333333333333333L);
			b = (b + (b >> 4)) & 0x0f0f0f0f0f0f0f0fL;
			distance += (b * 0x0101010101010101L) >> 56;
		}

		return distance;
	}

	private float euclideanF32Distance(ImageHash hash) {
		float[] thisBits = this.getBitArrayAsFloat(), otherBits = hash.getBitArrayAsFloat();
		double product = 0;
		for (int i = 0; i < thisBits.length; i++) {
			float diff = thisBits[i] - otherBits[i];
			product += diff * diff;
		}
		return (float) Math.sqrt(product);
	}

	private double euclideanF64Distance(ImageHash hash) {
		double[] thisBits = this.getBitArrayAsDouble(), otherBits = hash.getBitArrayAsDouble();
		double product = 0;
		for (int i = 0; i < thisBits.length; i++) {
			double diff = thisBits[i] - otherBits[i];
			product += diff * diff;
		}
		return Math.sqrt(product);
	}

	public void assertComparable(ImageHash hash) throws IllegalArgumentException {
		if (!this.hashInformation.equals(hash.getHashInformation())) throw new IllegalArgumentException(
				"These two hashes are not comparable. Tried to compare this hash: " + this.toString()
						+ " against another hash: " + hash.toString() + " that did not have the same hashInformation.");

	}

	// Returns the contents of this hash's bits in hexadecimal.
	private char[] hexHash() {
		char[] encodedChars = new char[this.bits.length * 16];
		for (int i = 0; i < this.bits.length; i++) {
			long v = this.bits[i];
			int idx = i * 16;
			for (int j = 0; j < 16; j++) {
				encodedChars[idx + j] = intToHexChar[(int) ((v >>> ((15 - j) * 4)) & 0x0F)];
			}
		}
		return encodedChars;
	}

	// Deep equals for everything

	@Override
	public boolean equals(Object h) {
		if (h == null) return false;
		if (!(h instanceof ImageHash)) return false;
		if (!this.equalsIgnoreSource(h)) return false;

		String sauce = ((ImageHash) h).getSource();
		return ((sauce == null) || (sauce.equals("null"))) ? (this.source == null || this.source.equals("null"))
				: sauce.equals(this.source);
	}

	// Deep equals for everything but the source
	public boolean equalsIgnoreSource(Object h) {
		if (h == null) return false;
		if (!(h instanceof ImageHash)) return false;
		ImageHash o = (ImageHash) h;
		return (this.hashInformation.equals(o.getHashInformation()) && Arrays.equals(this.bits, o.getBitArray()));
	}

}
