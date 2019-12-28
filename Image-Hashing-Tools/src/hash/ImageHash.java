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
	private final int hashLength; // Will still work if not a multiple of 64. Just don't mess with those bits.
	private String source = null; // null appends as "null" with StringBuilder.

	final protected static char[] intToHexChar = "0123456789ABCDEF".toCharArray();// Convenience for toString()

	public ImageHash(IHashAlgorithm creator, BitSet hash) throws IllegalArgumentException {
		this.type = creator.getHashName();
		this.bits = hash.toLongArray();
		this.hashLength = creator.getHashLength();
	}

	public ImageHash(IHashAlgorithm creator, BitSet hash, String source) throws IllegalArgumentException {
		this.type = creator.getHashName();
		this.bits = hash.toLongArray();
		this.hashLength = creator.getHashLength();
		this.source = source;
	}

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

	public ImageHash(String hashName, BitSet hash, int hashLength) throws IllegalArgumentException {
		checkName(hashName);
		this.type = hashName;
		this.bits = hash.toLongArray();
		this.hashLength = hashLength;
	}

	public ImageHash(String hashName, BitSet hash, int hashLength, String source) throws IllegalArgumentException {
		checkName(hashName);
		this.type = hashName;
		this.bits = hash.toLongArray();
		this.hashLength = hashLength;
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
		return new ImageHash(type, longs, Integer.parseInt(length), source == "null" ? null : source);
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
			File parent = new File(imageHash.getParent());
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

	public boolean getBit(int bitIndex) throws ArrayIndexOutOfBoundsException {
		// Taken from BitSet docs
		// https://docs.oracle.com/javase/7/docs/api/java/util/BitSet.html#toLongArray%28%29
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

		for (int arr = 0; arr < this.bits.length; arr++) {
			long x = this.bits[arr] ^ other[arr];
			for (int i = 0; i < 64; i++) {
				distance += x & 1;
				x >>= 1;
			}
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

		if (this.source.compareTo(hash.getSource()) > 0) {
			return 1;
		}
		if (this.source.compareTo(hash.getSource()) < 0) {
			return -1;
		}

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
