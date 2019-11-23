package hash.implementations.slicehash;

import java.util.Arrays;

import utils.ImageUtils;

public class SliceCalculations {

	protected SliceCalculations() {
	}

	static void findMeasuresOfCenterForVerticalSlice(int[] slice, int sliceX, int sliceY,
			int[][][] verticalSliceCenters, int sliceIndex, MeasureOfCenter moc) {

		int[][] subslices = takeHorizontalSubslices(slice, sliceX, sliceY);
		setCentersWithSubslices(subslices, verticalSliceCenters, sliceIndex, moc);
	}

	static void findMeasuresOfCenterForHorizontalSlice(int[] slice, int sliceX, int sliceY,
			int[][][] horizontalSliceCenters, int sliceIndex, MeasureOfCenter moc) {

		int[][] subslices = takeVerticalSubslices(slice, sliceX, sliceY);
		setCentersWithSubslices(subslices, horizontalSliceCenters, sliceIndex, moc);
	}

	private static void setCentersWithSubslices(int[][] subslices, int[][][] centers, int sliceIndex,
			MeasureOfCenter moc) {

		int measureOffset = 0;
		int subsliceIndexOffset = 0;
		int i = 0;

		if (moc.hasMean() && moc.getMeanWeight() != 0) {
			if (moc.getMeanTrimPercentage() == 0) {
				int[] subslice;
				for (i = 0; i < subslices.length; i++) {
					subslice = subslices[i];
					centers[sliceIndex][measureOffset][subsliceIndexOffset++] = findMean(subslice);
				}
			} else {
				float trimPercentage = moc.getMeanTrimPercentage();
				int[] subslice;
				for (i = 0; i < subslices.length; i++) {
					subslice = subslices[i];
					centers[sliceIndex][measureOffset][subsliceIndexOffset++] = findMean(
							trimSubSlice(subslice, trimPercentage));
				}
			}

			measureOffset++;
			subsliceIndexOffset = 0;
		}
		if (moc.hasMedian() && moc.getMedianWeight() != 0) {
			int[] subslice;
			for (i = 0; i < subslices.length; i++) {
				subslice = subslices[i];
				centers[sliceIndex][measureOffset][subsliceIndexOffset++] = findMedian(subslice);
			}

			measureOffset++;
			subsliceIndexOffset = 0;
		}
		if (moc.hasMode() && moc.getModeWeight() != 0) {
			int[] subslice;
			for (i = 0; i < subslices.length; i++) {
				subslice = subslices[i];
				centers[sliceIndex][measureOffset][subsliceIndexOffset++] = findMode(subslice);
			}

			measureOffset++;
			subsliceIndexOffset = 0;
		}
		if (moc.hasRange() && moc.getRangeWeight() != 0) {
			int[] subslice;
			for (i = 0; i < subslices.length; i++) {
				subslice = subslices[i];
				centers[sliceIndex][measureOffset][subsliceIndexOffset++] = findRange(subslice);
			}

			measureOffset++;
			subsliceIndexOffset = 0;
		}
	}

	// To be called on horizontal slices
	private static int[][] takeVerticalSubslices(int[] slice, int sliceX, int sliceY) {
		// sliceX > sliceY

		int[][] subslices = new int[sliceX][sliceY];
		int x = 0, y = 0;
		int sliceYMinusOne = sliceY - 1;

		for (;;) {
			subslices[x][y] = slice[y * sliceX + x];
			if (y == sliceYMinusOne) {
				y = 0;
				x++;
				if (x == sliceX) {
					break;
				}
			} else {
				y++;
			}

		}

		for (int[] subslice : subslices) {
			Arrays.sort(subslice);
		}

		return subslices;
	}

	// To be called on vertical slices
	private static int[][] takeHorizontalSubslices(int[] slice, int sliceX, int sliceY) {
		// It's switched because this is the vertical slice.
		// sliceY > sliceX

		// The geometric intuition on what's going on here is exactly the same as
		// converting a 1d to a 2d.
		int[][] subslices = ImageUtils.array1dToArray2d(slice, sliceX, sliceY);

		for (int[] subslice : subslices) {
			Arrays.sort(subslice);
		}

		return subslices;
	}

	// Assume for all methods below that the slice is already sorted. This is done
	// in takeVertical|HorizontalSubslices.

	// Trim percentage comes off both sides.
	private static int[] trimSubSlice(int[] subSlice, float trimPercentage) {
		int trimOffEachSide = (int) Math.ceil(subSlice.length * trimPercentage);
		int[] newSlice = Arrays.copyOfRange(subSlice, trimOffEachSide, subSlice.length - trimOffEachSide);
		return newSlice;
	}

	private static int findMean(int[] subSlice) {
		long sum = 0;
		for (int i = 0; i < subSlice.length; i++) {
			sum += subSlice[i];
		}
		return (int) (sum / subSlice.length);
	}

	private static int findMedian(int[] subSlice) {
		int middle = subSlice.length / 2;
		return subSlice.length % 2 == 1 ? subSlice[middle] : (subSlice[middle - 1] + subSlice[middle]) / 2;
	}

	// If there's more than one mode, return the smallest one.
	private static int findMode(int[] subSlice) {
		int maxValue = 0, maxCount = 0, count = 0;

		for (int i = 0; i < subSlice.length; i++) {
			if (i > 0 && subSlice[i] != subSlice[i - 1]) {
				count = 0;
			}
			if (++count > maxCount) {
				maxValue = subSlice[i];
				maxCount = count;
			}
		}

		return maxValue;
	}

	private static int findRange(int[] subSlice) {
		return subSlice[subSlice.length - 1] - subSlice[0];
	}
}
