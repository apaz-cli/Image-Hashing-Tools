package hash.implementations;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.BitSet;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.implementations.YCbCrImage;

public class SliceHash implements IHashAlgorithm {

	@Override
	public String getHashName() {
		return "sHash";
	}

	@Override
	public boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode) {
		// Assert comparable
		if (mode == MatchMode.SLOPPY) {
			return hash1.hammingDistance(hash2) < 3;
		} else if (mode == MatchMode.NORMAL) {
			return hash1.hammingDistance(hash2) < 2;
		} else if (mode == MatchMode.STRICT) {
			return hash1.hammingDistance(hash2) < 1;
		}
		// MatchMode.EXACT
		return hash1.hammingDistance(hash2) == 0;
	}

	private static final int imageSideLength = 32;
	private static final int sliceNumber = 8;
	private static final int sliceWidth = imageSideLength / sliceNumber; // 32/8 = 4

	private static final float averageWeight = 1 / 3f;
	private static final float medianWeight = 2 / 3f;

	@Override
	public ImageHash hash(IImage<?> img) {
		// Resize the image down to 32x32 (or whatever the image side length is set to),
		// and take its luminance.
		img = img.resizeBilinear(imageSideLength, imageSideLength);
		byte[] luminanceThumbnail = img.toYCbCr().getY().getPixels();

		int[] horizontalMedians = new int[sliceNumber];
		int[] verticalMedians = new int[sliceNumber];
		int[] horizontalAverages = new int[sliceNumber];
		int[] verticalAverages = new int[sliceNumber];
		int average; // Reused in loops to calculate averages

		// ((2/3) * Median) + ((1/3) * Average) indexed horizontal, then vertical
		int[] weightedCenters = new int[sliceNumber * 2];

		// This is its own variable so that the JVM can fill in and interpret it as a
		// constant.
		// The slice size is the total number of pixels in the slice.
		int sliceSize = imageSideLength * sliceWidth;
		int[] slice = new int[sliceSize];

		// These seem benign, but I don't want to have to keep doing these inside my
		// loops. No declarations in loops is best.
		int middle = sliceSize / 2;

		int sliceOffset = 0;
		int mocOffset = 0; // Measure of Center offset
		int x = 0, y = 0; // width/height inside luminanceThumbnail
		int i; // index for iterating through Averages/Medians

		// Take median of each horizontally sliced eighth of the image.
		for (; y < imageSideLength;) {
			// Take slice
			for (; sliceOffset < sliceSize;) {
				slice[sliceOffset++] = luminanceThumbnail[y * imageSideLength + x] & 0xff;
				if (x == imageSideLength - 1) {
					x = 0;
					y++;
					if (y % sliceWidth == 0) {
						sliceOffset = 0;
						break;
					}
				} else {
					x++;
				}
			}

			// Find median of slice, append
			Arrays.sort(slice, 0, sliceSize);
			horizontalMedians[mocOffset] = sliceSize % 2 == 0
					? ((slice[middle] & 0xff) + (slice[middle + 1] & 0xff)) / 2
					: (slice[middle + 1] & 0xff);

			// Find average of slice, append
			average = 0;
			for (i = 0; i < sliceSize; i++) {
				average += slice[i];
			}
			average /= (sliceWidth * imageSideLength);
			horizontalAverages[mocOffset++] = average;
		}

		// Reset indexes
		sliceOffset = 0;
		mocOffset = 0;
		x = 0;
		y = 0;

		// Take median of each horizontally sliced eighth of the image.
		for (; x < imageSideLength;) {
			for (; sliceOffset < sliceSize;) {
				slice[sliceOffset++] = luminanceThumbnail[y * imageSideLength + x] & 0xff;
				if (y == imageSideLength - 1) {
					y = 0;
					x++;
					if (x % sliceWidth == 0) {
						sliceOffset = 0;
						break;
					}
				} else {
					y++;
				}
			}

			Arrays.sort(slice, 0, sliceSize);
			verticalMedians[mocOffset] = sliceSize % 2 == 0 ? ((slice[middle] & 0xff) + (slice[middle + 1] & 0xff)) / 2
					: (slice[middle + 1] & 0xff);

			average = 0;
			for (i = 0; i < sliceSize; i++) {
				average += slice[i];
			}
			average /= (sliceWidth * imageSideLength);
			verticalAverages[mocOffset++] = average;
		}

		// Calculate weighted centers based on averages and medians
		// @nof
		mocOffset = 0;
		for (; mocOffset < sliceNumber; mocOffset++) {
			weightedCenters[mocOffset] = (int) ((averageWeight * (horizontalAverages[mocOffset]))
					+ (medianWeight * horizontalMedians[mocOffset]));
			weightedCenters[mocOffset + sliceNumber] = (int) ((averageWeight * (verticalAverages[mocOffset]))
					+ (medianWeight * verticalMedians[mocOffset]));
		}
		// @dof

		System.out.println(Arrays.toString(weightedCenters));

		// Set digits of hash.
		// JVM replaces constants in for loop
		mocOffset = 0;
		BitSet bs = new BitSet((sliceNumber * 2) - 2);
		for (; mocOffset < sliceNumber - 1; mocOffset++) {
			if (weightedCenters[mocOffset] > weightedCenters[mocOffset + 1]) {
				bs.set(mocOffset);
			}
		}
		for (; mocOffset < (2 * sliceNumber) - 1; mocOffset++) {
			if (weightedCenters[mocOffset] > weightedCenters[mocOffset + 1]) {
				bs.set(mocOffset);
			}
		}
		
		System.out.println(bs); //TODO WUT
		return new ImageHash(bs, this.getHashName());
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		return this.hash(new YCbCrImage(img));
	}

}
