package hash.implementations.slicehash;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.BitSet;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.implementations.YCbCrImage;

public class SliceHash implements IHashAlgorithm {

	private final int imageSideLength;
	private final int sliceNumber;
	private final int sliceWidth;
	private final MeasureOfCenter moc;

	private final int imageSideLengthMinusOne;

	// Defaults to 8 slices of a 32x32 image, with MOC = untrimmed mean.
	public SliceHash() {
		this.imageSideLength = 32;
		this.sliceNumber = 8;
		this.sliceWidth = 4;// sliceWidth = imageSideLength / sliceNumber and 32/8 = 4
		// Use raw mean as measure of center by default.
		this.moc = new MeasureOfCenter(true, false, false);

		// This seems benign, but I don't want to have to keep doing these inside my
		// loops. No declarations in loops is best.
		this.imageSideLengthMinusOne = this.imageSideLength - 1;
	}

	public SliceHash(int imageSideLength, int sliceNumber, MeasureOfCenter moc) throws IllegalArgumentException {
		if ((imageSideLength % sliceNumber) != 0) {
			throw new IllegalArgumentException(
					"The number of slices must divide the image side length. sliceNumber Was: " + sliceNumber
							+ ". imageSideLength was: " + imageSideLength);
		}

		this.imageSideLength = imageSideLength;
		this.sliceNumber = sliceNumber;
		this.sliceWidth = this.imageSideLength / this.sliceNumber;
		this.moc = moc;

		// This seems benign, but I don't want to have to keep doing these inside my
		// loops. No declarations in loops is best.
		this.imageSideLengthMinusOne = this.imageSideLength - 1;
	}

	@Override
	public String getHashName() {
		return "sHash" + this.imageSideLength + "with" + this.sliceNumber + "slices";
	}

	@Override
	public boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode) {
		// TODO change to based on length of hashes

		// Hamming distance method asserts comparable. So, their lengths will be the
		// same, or an exception will be thrown.
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

	@Override
	public ImageHash hash(IImage<?> img) {
		// Resize the image down to 32x32 (or whatever the image side length is set to),
		// and take its luminance.
		IImage<?> resized = img.resizeBilinear(this.imageSideLength, this.imageSideLength);
		byte[] imageThumbnail = resized.toGreyscale().getPixels();

		int numberOfMeasuresOfCenter = moc.getLengthModifier();
		// [Slice that the measures belong to][# of Measures of center, specified by
		// this.moc][resized image side length]
		int[][][] horizontalSliceCenters = new int[this.sliceNumber][numberOfMeasuresOfCenter][this.imageSideLength];
		int[][][] verticalSliceCenters = new int[this.sliceNumber][numberOfMeasuresOfCenter][this.imageSideLength];

		int[][] combinedHorizontalCenters = new int[2 * this.sliceNumber][this.imageSideLength];
		int[][] combinedVerticalCenters = new int[2 * this.sliceNumber][this.imageSideLength];
		int currentMeasure;

		// This is its own variable so that the JVM can fill in and interpret it as a
		// constant.
		// The slice size is the total number of pixels in the slice.
		int sliceSize = this.imageSideLength * this.sliceWidth;

		int sliceOffset = 0;
		int x = 0, y = 0; // width/height inside imageThumbnail
		int currentSlicenumber = 0;
		int[] slice = new int[sliceSize];

		// Take measures of center of each horizontally sliced eighth of the image.
		for (; y < this.imageSideLength;) {
			// Take slice
			for (; sliceOffset < sliceSize;) {
				slice[sliceOffset++] = imageThumbnail[y * this.imageSideLength + x] & 0xff;
				if (x == this.imageSideLengthMinusOne) {
					x = 0;
					y++;
					if (y % this.sliceWidth == 0) {
						sliceOffset = 0;
						break;
					}
				} else {
					x++;
				}
			}

			// For each center, pass the slice to SliceCalculations with MOC.
			// This will take the measures of center that have been calculated, and store
			// them back.
			SliceCalculations.findMeasuresOfCenterForHorizontalSlice(slice, this.imageSideLength, this.sliceWidth,
					horizontalSliceCenters, currentSlicenumber++, moc);
		}

		// Reset indexes
		sliceOffset = 0;
		x = 0;
		y = 0;
		currentSlicenumber = 0;

		// Take measures of center of each vertically sliced eighth of the image.
		for (; currentSlicenumber < this.sliceNumber;) {
			for (;;) {
				slice[sliceOffset++] = imageThumbnail[y * this.imageSideLength + x] & 0xff;

				if (x == (this.sliceWidth * currentSlicenumber + this.sliceWidth - 1)) {
					x = (currentSlicenumber * this.sliceWidth);
					y++;

					if (y == this.imageSideLengthMinusOne) {
						y = 0;
						sliceOffset = 0;
						break;
					}
				} else {
					x++;
				}

			}

			SliceCalculations.findMeasuresOfCenterForVerticalSlice(slice, this.sliceWidth, this.imageSideLength,
					verticalSliceCenters, currentSlicenumber++, moc);
		}

		// Weight the measures of center that have been calculated.
		for (currentSlicenumber = 0; currentSlicenumber < this.sliceNumber; currentSlicenumber++) {
			float[] weighted = new float[this.imageSideLength];
			for (int i = 0; i < weighted.length; i++) {
				currentMeasure = 0;
				if (this.moc.hasMean()) {
					weighted[i] += horizontalSliceCenters[currentSlicenumber][currentMeasure++][i];
				}
				if (this.moc.hasMedian()) {
					weighted[i] += horizontalSliceCenters[currentSlicenumber][currentMeasure++][i];
				}
				if (this.moc.hasMode()) {
					weighted[i] += horizontalSliceCenters[currentSlicenumber][currentMeasure++][i];
				}
				if (this.moc.hasRange()) {
					weighted[i] += horizontalSliceCenters[currentSlicenumber][currentMeasure++][i];
				}
			}

			int[] casted = new int[this.imageSideLength];
			for (int i = 0; i < casted.length; i++) {
				// +.5f and cast (truncate) has the effect of rounding to the nearest integer.
				casted[i] = (int) (weighted[i] + .5f);
			}
			combinedHorizontalCenters[currentSlicenumber] = casted;
		}
		for (; currentSlicenumber < this.sliceNumber * 2; currentSlicenumber++) {

		}

		// Set digits of hash.
		// JVM replaces constants in for loop
		BitSet bs = new BitSet((this.sliceNumber * 2) - 2);

		return new ImageHash(this.getHashName(), bs, (this.sliceNumber * 2) - 2);
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		return this.hash(new YCbCrImage(img));
	}

}
