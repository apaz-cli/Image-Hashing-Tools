package hash;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.BitSet;

public class ImageHash implements Comparable<ImageHash>, Serializable {

	private static final long serialVersionUID = 1L;

	// TODO rewrite bits as Integer for bitLength 32, and Long for bitLength 64.
	// It should behave as normal otherwise.

	private final BitSet bits;
	private final String type;
	private final int bitLength;
	private String source = null;

	public ImageHash(String hashType, BitSet hash, int bitLength) throws IllegalArgumentException {
		char nameChar;
		for (int i = 0; i < hashType.length(); i++) {
			nameChar = hashType.charAt(i);
			if (nameChar == ',' || nameChar == '"' || nameChar == '|') {
				throw new IllegalArgumentException(
						"Hash type may not contain the characters comma, pipe, or double quote (,|\").");
			}
		}
		this.bitLength = bitLength;
		this.bits = hash;
		this.type = hashType == null ? "unknownHash" : hashType;
	}

	public ImageHash(String hashType, BitSet hash, int bitLength, String source) throws IllegalArgumentException {
		this(hashType, hash, bitLength);
		this.source = this.source == "null" ? null : source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return this.source;
	}

	public BitSet getBits() {
		return bits;
	}

	public boolean getBit(int bitIndex) throws ArrayIndexOutOfBoundsException {
		return bits.get(bitIndex);
	}

	public int getLength() {
		return this.bitLength;
	}

	public String getType() {
		return type;
	}

	public int hammingDistance(ImageHash hash) throws IllegalArgumentException {
		areComparable(hash);

		int distance = 0;
		BitSet otherBitSet = hash.getBits();
		for (int i = 0; i < this.bits.length(); i++) {
			if (this.bits.get(i) != otherBitSet.get(i)) {
				distance++;
			}
		}

		return distance;
	}

	@Override
	public int compareTo(ImageHash hash) throws IllegalArgumentException {
		areComparable(hash);

		// If they're just straight up equal, then say so.
		BitSet otherBitSet = hash.getBits();
		if (this.bits.equals(otherBitSet)) {
			return 0;
		}

		// If they're different somewhere, report which is smaller
		for (int i = 0; i < this.bits.length(); i++) {
			if (this.bits.get(i) != otherBitSet.get(i)) {
				return this.bits.get(i) ? 1 : -1;
			}
		}

		// If they're not different anywhere, then they're bitwise equal.
		return 0;
	}

	// For easy serialization
	// 1. length,bits
	// 2. length,bits,source
	// 3. type,length,bits,source
	@Override
	public String toString() {
		// BitSet to String of 1-0s
		StringBuilder bitStr = new StringBuilder();
		for (int i = 0; i < this.bits.length(); i++) {
			bitStr.append(this.bits.get(i) ? "1" : "0");
		}

		// Pad out to correct length so that hex conversion works
		while (bitStr.length() < bitLength) {
			bitStr.append("0");
		}

		// Convert (potentially very large) bit string to hex
		String hexStr = new BigInteger(bitStr.toString(), 2).toString(16);

		return this.type + "," + this.bitLength + "," + hexStr + "," + (this.source != null ? this.source : "null");
	}

	// For easy serialization
	// 3. type,length,bits,source
	public String toString(String source) throws IllegalArgumentException {

		if (source != null) {
			char c;
			for (int i = 0; i < source.length(); i++) {
				c = source.charAt(i);
				if (c == '"' || c == '|') {
					throw new IllegalArgumentException(
							"Image source may not contain the characters pipe, or double quote (|\").");
				}
			}
		}

		StringBuilder bitStr = new StringBuilder();
		for (int i = 0; i < this.bits.length(); i++) {
			bitStr.append(this.bits.get(i) ? "1" : "0");
		}

		// Pad out to correct length so that hex conversion works
		while (bitStr.length() < bitLength) {
			bitStr.append("0");
		}

		// Convert (potentially very large) bit string to hex
		String hexStr = new BigInteger(bitStr.toString(), 2).toString(16);

		return this.type + "," + this.bitLength + "," + hexStr + "," + (source != null ? source : "null");
	}

	public static ImageHash fromString(String imageHash) throws IllegalArgumentException {
		// Can deserialize in three different forms
		// (bits are in hex, and must be converted to decimal string)
		// 1. length,bits
		// 2. length,bits,source
		// 3. type,length,bits,source

		// Parse for arguments
		String type = null, length = null, bits = null, source = null;
		if (!imageHash.contains(",")) {
			throw new IllegalArgumentException("Could not parse hash, no commas.");
		} else {
			String[] split = imageHash.split(",");

			if (split.length == 2) {
				length = split[0];
				bits = split[1];
			} else if (split.length == 3) {
				length = split[0];
				bits = split[1];
				source = split[2];
			} else {
				type = split[0];
				length = split[1];
				bits = split[2];
				// Handles commas in URLs
				source = "";
				for (int i = 3; i < split.length; i++) {
					source += split[i];
				}
			}
		}

		// Convert hash from hex string to bitset.
		String binHash = "";
		try {
			binHash = Integer.toString(Integer.parseInt(bits, 16), 2);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("The hash could not be parsed from this string.");
		}
		BitSet bs = new BitSet(binHash.length());
		for (int i = 0; i < binHash.length(); i++) {
			bs.set(i, binHash.charAt(i) == 1);
		}

		// Clean up if source is null
		if (source != null) {
			if (source == "null") {
				source = null;
			}
		}

		// If in normal form, it begins with the name. If it was just the hex bits, then
		// give it a name.
		return new ImageHash(type, bs, Integer.parseInt(length), source);
	}

	// Dependent only on the bits
	@Override
	public boolean equals(Object h) {
		if (h instanceof ImageHash) {
			return this.bits.equals(((ImageHash) h).getBits());
		}
		return false;
	}

	// Dependent only on the bits
	@Override
	public int hashCode() {
		return this.bits.hashCode();
	}

	public float percentHammingDifference(ImageHash hash) throws IllegalArgumentException {
		areComparable(hash);

		int distance = this.hammingDistance(hash);
		return distance / (float) this.bits.length();
	}

	private void areComparable(ImageHash hash) throws IllegalArgumentException {
		// If one or both of them is of unknown origin, then ignore comparison of types.
		if (!(this.type == "unknownHash" || hash.type == "unknownHash")) {
			if (this.type != hash.getType()) {
				throw new IllegalArgumentException(
						"These two hashes are not the same type, and therefore cannot be compared.");
			}
		}

		if (this.bitLength != hash.getLength()) {
			throw new IllegalArgumentException(
					"These two hashes are not of the same length, and therefore cannot be compared. Hash 1: "
							+ this.getLength() + " Hash 2: " + hash.getLength());
		}
	}
}
