package hash;

import java.io.Serializable;
import java.util.BitSet;

public class ImageHash implements Comparable<ImageHash>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private final BitSet bits;
	private final String type;

	public ImageHash(BitSet hash, String hashType) throws IllegalArgumentException {
		char nameChar;
		for (int i = 0; i < hashType.length(); i++) {
			nameChar = hashType.charAt(i);
			if (nameChar == ',' || nameChar == '"' || nameChar == '|') {
				throw new IllegalArgumentException(
						"Hash type may not contain the characters comma, pipe, or double quote (,|\").");
			}
		}

		this.bits = hash;
		this.type = hashType;
	}

	public BitSet getBits() {
		return bits;
	}

	public int length() {
		return bits.length();
	}

	public boolean getBit(int bitIndex) throws ArrayIndexOutOfBoundsException {
		return bits.get(bitIndex);
	}

	public int getLength() {
		return this.bits.length();
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
				return !this.bits.get(i) ? -1 : 1;
			}
		}

		// If they're not different anywhere, then they're bitwise equal.
		return 0;
	}
	
	// For easy serialization
	// In the form type,bits
	@Override
	public String toString() {
		StringBuilder bitStr = new StringBuilder();
		for (int i = 0; i < this.bits.length(); i++) {
			bitStr.append(this.bits.get(i) ? 1 : 0);
		}
		
		// Convert bit string to hex
		String hexStr = Integer.toString(Integer.parseInt(bitStr.reverse().toString(),2),16);
		return this.type + "," + hexStr;
	}

	// For easy serialization
	// In the form type,bits,name
	public String toString(String imagePath) throws IllegalArgumentException {
		char c;
		for (int i = 0; i < imagePath.length(); i++) {
			c = imagePath.charAt(i);
			if (c == ',' || c == '"' || c == '|') {
				throw new IllegalArgumentException(
						"Image path may not contain the characters comma, pipe, or double quote (,|\").");
			}
		}
		
		return this.toString() + "," + imagePath;
	}

	public static ImageHash fromString(String imageHash) throws IllegalArgumentException {
		// Can deserialize in three different forms
		// (bits are in hex, and must be converted to decimal string)
		// 1. bits
		// 2. Name,bits
		// 3. Name,bits,path
		
		String[] split;
		int hashIndex;
		if (imageHash.contains(",")) {
			hashIndex = 1;
			split = imageHash.split(",");
		} else {
			hashIndex = 0;
			split = new String[1];
			split[0] = imageHash;
		}
		
		String binHash = "";
		try {
			binHash = Integer.toString(Integer.parseInt(split[hashIndex], 16), 2);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("The hash could not be parsed from this string.");
		}
		
		
		BitSet bs = new BitSet(binHash.length());
		for (int i = 0; i < binHash.length(); i++) {
			bs.set(i, binHash.charAt(i) == 1);
		}
		
		// If in normal form, it begins with the name. If it was just the hex bits, then give it a name.
		return new ImageHash(bs, split.length >= 2 ? split[0] : "unknownHash");
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

	public float percentSimilarity(ImageHash hash) throws IllegalArgumentException {
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
		
		if (this.bits.length() != hash.getLength()) {
			throw new IllegalArgumentException(
					"These two hashes are not of the same length, and therefore cannot be compared.");
		}
	}
}
