package hash;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

public class ImageHash implements Comparable<ImageHash>, Serializable {

	private static final long serialVersionUID = 1L;

	private final long[] bits;
	private final String type; // name null is not legal. Name also cannot contain certain characters. (,|\")
	private final int hashLength; // Will still work if not a multiple of 64. We just don't mess with those bits.
	private String source = null; // null appends as "null" with StringBuilder in toString(), which effectively
									// serializes hashes.

	final protected static char[] intToHexChar = "0123456789ABCDEF".toCharArray();// Convenience for toString()

	public ImageHash(IHashAlgorithm creator, long[] hash) throws IllegalArgumentException {
		this.type = creator.getHashName();
		this.bits = Arrays.copyOf(hash, hash.length);
		this.hashLength = creator.getHashLength();
	}

	public ImageHash(IHashAlgorithm creator, long[] hash, String source) throws IllegalArgumentException {
		this.type = creator.getHashName();
		this.bits = Arrays.copyOf(hash, hash.length);
		this.hashLength = creator.getHashLength();
		this.source = source;
	}

	public ImageHash(String hashName, long[] hash, int hashLength) throws IllegalArgumentException {
		checkName(hashName);
		this.type = hashName;
		this.bits = Arrays.copyOf(hash, hash.length);
		this.hashLength = hashLength;
	}

	public ImageHash(String hashName, long[] hash, int hashLength, String source) throws IllegalArgumentException {
		checkName(hashName);
		this.type = hashName;
		this.bits = Arrays.copyOf(hash, hash.length);
		this.hashLength = hashLength;
		this.source = source;
	}

	// Hash name may not contain the characters comma, pipe, or double quote (,|\").
	// To save time, we don't check when we're using an IHashAlgorithm. If somebody
	// implements it wrong, that's on them.
	private void checkName(String hashName) throws IllegalArgumentException {
		for (char c : hashName.toCharArray()) {
			if (c == '|' || c == ',' || c == '\"') {
				throw new IllegalArgumentException(
						"Hash name may not contain the characters comma, pipe, or double quote (,|\").");
			}
		}
	}

	public static ImageHash fromString(String imageHash) throws NumberFormatException, IllegalArgumentException {
		// Can deserialize in two different forms
		// 1. type,length,bits
		// 2. type,length,bits,source

		// Parse for arguments
		String type = null, length = null, bits = null, source = null;
		String[] split = imageHash.split(",");

		if (split.length == 3) {
			type = split[0];
			length = split[1];
			bits = split[2];
		} else if (split.length == 4) {
			type = split[0];
			length = split[1];
			bits = split[2];
			source = split[3];
		} else {
			type = split[0];
			length = split[1];
			bits = split[2];
			// Joining the trailing splits handles possible commas in URLs.
			source = "";
			for (int i = 3; i < split.length; i++) {
				source += split[i];
			}
		}

		if (type == null) {
			throw new IllegalArgumentException("Not enough commas. Can deserialize in two different forms:\n"
					+ "1. type,length,bits\n" + "2. type,length,bits,source");
		}

		long[] longs = new long[(Integer.valueOf(length) + 63) / 64];

		int currentLong = 0, nibbleOffset = 0;
		int c;
		for (char hexChar : (bits = bits.toUpperCase()).toCharArray()) {
			c = hexChar - 48;
			if (c < 0x0) {
				throw new IllegalArgumentException(
						"Hash contains non-hex characters. Can deserialize in two different forms:\n"
								+ "1. type,length,bits\n" + "2. type,length,bits,source");
			} else if (c > 0x9) {
				c -= 7;
				if (c < 0xA || c > 0xF) {
					throw new IllegalArgumentException(
							"Hash contains non-hex characters. Can deserialize in two different forms:\n"
									+ "1. type,length,bits\n" + "2. type,length,bits,source");
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

		// If in normal form, it begins with the name. If it was just the hex bits, then
		// give it a name.
		return new ImageHash(type, longs, Integer.parseInt(length), source.equals("null") ? null : source);
	}

	public static ImageHash fromFile(File imageHash) throws IOException {
		DataInputStream dis = new DataInputStream(new FileInputStream(imageHash));
		String hash = dis.readUTF();
		dis.close();
		return ImageHash.fromString(hash);
	}

	public void writeToFile(File imageHash) throws IOException, FileNotFoundException {
		if (!imageHash.isFile()) {
			throw new FileNotFoundException(
					"File passed could be found, but is not a file, and is probably a directory.");
		}
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(imageHash, false));
		dos.writeUTF(this.toString());
		dos.close();
	}

	public void writeToNewFile(File imageHash) throws IOException, FileNotFoundException {
		if (!imageHash.exists()) {
			String p = imageHash.getParent();
			File parent = p == null ? null : new File(p);
			if (parent != null) {
				if (!parent.exists()) {
					if (!parent.mkdirs()) {
						throw new IOException("Directories leading up to this file could not be created.");
					}
				}
			}
			// Cannot return false, and throws own exception if there's a problem.
			imageHash.createNewFile();
		}
		this.writeToFile(imageHash);
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return this.source;
	}

	public BitSet getBits() {
		return BitSet.valueOf(this.bits);
	}

	public long[] getBitArray() {
		return this.bits;
	}

	/* Euclidean */

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
		return (this.hashLength % 2 == 0) ? ibits : Arrays.copyOf(ibits, (this.bits.length * 2) - 1);
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
		// Taken from BitSet docs
		// https://docs.oracle.com/javase/7/docs/api/java/utils/BitSet.html#toLongArray%28%29
		return ((this.bits[bitIndex / 64] & (1L << (bitIndex % 64))) != 0);
	}

	public int getLength() {
		return this.hashLength;
	}

	public String getType() {
		return type;
	}

	public int hammingDistance(ImageHash hash) throws IllegalArgumentException {
		areDistanceComparable(hash);

		// While there may be some unused bits at the end of the last long, since we
		// aren't touching them they'll be the same. It isn't worth trying to optimize
		// it away. Most hashes are a multiple of 64 long, and it isn't worth checking.
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

	public float percentHammingDifference(ImageHash hash) throws IllegalArgumentException {
		areDistanceComparable(hash);

		int distance = this.hammingDistance(hash);
		return distance / (float) this.hashLength;
	}

	private void areDistanceComparable(ImageHash hash) throws IllegalArgumentException {
		// If one or both of them is of unknown origin, then ignore comparison of types.
		if (!(this.type == "unknownHash" || hash.type == "unknownHash")) {
			if (this.type != hash.getType()) {
				throw new IllegalArgumentException(
						"These two hashes are not the same type, and therefore cannot be compared.");
			}
		}

		if (this.hashLength != hash.getLength()) {
			throw new IllegalArgumentException(
					"These two hashes are not of the same length, and therefore cannot be compared. Hash 1: "
							+ this.getLength() + " Hash 2: " + hash.getLength());
		}
	}

	// Sort alphabetically by algorithm, then by hash length least to greatest, then
	// alphabetically by source, then by hash, least to greatest numerically.
	@Override
	public int compareTo(ImageHash hash) throws IllegalArgumentException {

		if (this.type.compareTo(hash.getType()) > 0) {
			return 1;
		}

		if (this.type.compareTo(hash.getType()) < 0) {
			return -1;
		}

		if (this.hashLength != hash.getLength()) {
			return this.hashLength > hash.getLength() ? 1 : -1;
		}

		String hSource = hash.getSource();
		if (this.source != null && hSource != null) {
			if (this.source.compareTo(hash.getSource()) > 0) {
				return 1;
			}
			if (this.source.compareTo(hash.getSource()) < 0) {
				return -1;
			}
		}

		// null sources are considered less than.
		if (this.source == null && hSource != null) {
			return -1;
		} else if (hSource == null && this.source != null) {
			return 1;
		}

		// If both are null, continue on and sort by hash.

		long[] other = hash.getBitArray();

		long x, y;
		for (int arr = 0; arr < this.bits.length; arr++) {
			x = bits[arr];
			y = other[arr];
			for (int i = 0; i < 64; i++) {
				if ((x & 1) != (y & 1)) {
					return (y & 1) == 1 ? -1 : 1;
				}
				x >>= 1;
				y >>= 1;
			}
		}

		// If they're bitwise equal, report equal.
		return 0;
	}

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

	// type,length,bits,source
	@Override
	public String toString() {
		// @nof
		return new StringBuilder(this.type)
				.append(",")
				.append(this.hashLength)
				.append(",")
				.append(this.hexHash())
				.append(",")
				.append(this.source)
				.toString();
		// @dof
	}

	// Same as above, but overwrites source
	public String toString(String source) throws IllegalArgumentException {
		// @nof
		return new StringBuilder(this.type)
				.append(",")
				.append(this.hashLength)
				.append(",")
				.append(this.hexHash())
				.append(",")
				.append(source)
				.toString();
		// @dof
	}

	// Deep equals
	@Override
	public boolean equals(Object h) {
		if (h instanceof ImageHash) {
			ImageHash o = (ImageHash) h;
			return this.compareTo(o) == 0;
		}
		return false;
	}

	// Dependent only on the bits
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.bits);
	}

}
