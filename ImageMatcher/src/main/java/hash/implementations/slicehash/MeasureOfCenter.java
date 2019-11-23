package hash.implementations.slicehash;

public class MeasureOfCenter {

	private boolean mean = false;
	private boolean median = false;
	private boolean mode = false;
	private boolean range = false;

	private float trimPercentageMean = 0;

	private float meanWeight = 0;
	private float medianWeight = 0;
	private float modeWeight = 0;
	private float rangeWeight = 0;

	public MeasureOfCenter(boolean mean, boolean median, boolean mode) {
		this.mean = mean;
		this.median = median;
		this.mode = mode;
		this.calculateDefaultWeights();
	}

	public MeasureOfCenter(boolean mean, boolean median, boolean mode, boolean range) {
		this.mean = mean;
		this.median = median;
		this.mode = mode;
		this.range = range;
		this.calculateDefaultWeights();
	}

	public MeasureOfCenter(boolean mean, boolean median, boolean mode, float trimPercentageMean) {
		this.mean = mean;
		this.median = median;
		this.mode = mode;
		this.verifyTrimPercentage(trimPercentageMean);
		this.trimPercentageMean = trimPercentageMean;
		this.calculateDefaultWeights();
	}

	public MeasureOfCenter(boolean mean, float trimPercentageMean, boolean median, boolean mode) {
		this(mean, median, mode, trimPercentageMean);
	}

	public MeasureOfCenter(boolean mean, boolean median, boolean mode, boolean range, float trimPercentageMean) {
		this.mean = mean;
		this.median = median;
		this.mode = mode;
		this.range = range;
		this.verifyTrimPercentage(trimPercentageMean);
		this.trimPercentageMean = trimPercentageMean;
		this.calculateDefaultWeights();
	}

	public MeasureOfCenter(boolean mean, float trimPercentageMean, boolean median, boolean range, boolean mode) {
		this(mean, median, mode, range, trimPercentageMean);
	}

	public MeasureOfCenter(boolean mean, float meanWeight, boolean median, float medianWeight, boolean mode,
			float modeWeight) {
		this(mean, meanWeight, median, medianWeight, mode, modeWeight, false, 0f);
	}

	public MeasureOfCenter(boolean mean, float meanWeight, float trimPercentageMean, boolean median, float medianWeight,
			boolean mode, float modeWeight) {
		this(mean, meanWeight, trimPercentageMean, median, medianWeight, mode, modeWeight, false, 0f);
	}

	public MeasureOfCenter(boolean mean, float meanWeight, boolean median, float medianWeight, boolean mode,
			float modeWeight, boolean range, float rangeWeight) {
		this(mean, meanWeight, 0f, median, medianWeight, mode, modeWeight, range, rangeWeight);
	}

	public MeasureOfCenter(boolean mean, float meanWeight, float trimPercentageMean, boolean median, float medianWeight,
			boolean mode, float modeWeight, boolean range, float rangeWeight) {

		verifyWeights(mean, median, mode, range, meanWeight, medianWeight, modeWeight, rangeWeight);
		this.verifyTrimPercentage(trimPercentageMean);

		this.mean = mean;
		this.median = median;
		this.mode = mode;
		this.range = range;

		this.trimPercentageMean = trimPercentageMean;

		this.meanWeight = meanWeight;
		this.medianWeight = medianWeight;
		this.modeWeight = modeWeight;
		this.rangeWeight = rangeWeight;

	}

	private void calculateDefaultWeights() {
		int len = this.getLengthModifier();
		this.meanWeight = this.mean ? 1f / len : 0;
		this.medianWeight = this.median ? 1f / len : 0;
		this.modeWeight = this.mode ? 1f / len : 0;
		this.rangeWeight = this.range ? 1f / len : 0;
	}

	public boolean hasMean() {
		return this.mean && this.meanWeight != 0;
	}

	public boolean hasMedian() {
		return this.median && this.medianWeight != 0;
	}

	public boolean hasMode() {
		return this.mode && this.modeWeight != 0;
	}

	public boolean hasRange() {
		return this.range && this.rangeWeight != 0;
	}

	public float getMeanWeight() {
		return meanWeight;
	}

	public float getMedianWeight() {
		return medianWeight;
	}

	public float getModeWeight() {
		return modeWeight;
	}

	public float getRangeWeight() {
		return rangeWeight;
	}

	public float getMeanTrimPercentage() {
		return this.trimPercentageMean;
	}

	public int getLengthModifier() {
		int len = 0;
		if (this.mean && this.meanWeight != 0) {
			len += 1;
		}
		if (this.median && this.medianWeight != 0) {
			len += 1;
		}
		if (this.mode && this.modeWeight != 0) {
			len += 1;
		}
		if (this.range && this.rangeWeight != 0) {
			len += 1;
		}
		return len;
	}

	public int getSliceMOCLength(int sliceLength) {
		return this.getLengthModifier() * sliceLength;
	}

	private void verifyTrimPercentage(float trimPercentage) throws IllegalArgumentException {
		if (trimPercentage < 0) {
			throw new IllegalArgumentException("The trim percentage cannot be less than zero. Was: " + trimPercentage);
		}
		if (trimPercentage > 1) {
			throw new IllegalArgumentException(
					"The trim percentage cannot be greater than 1, which corresponds to 100%. Was: " + trimPercentage);
		}
	}

	private void verifyWeights(boolean mean, boolean median, boolean mode, boolean range, float meanWeight,
			float medianWeight, float modeWeight, float rangeWeight) {

		float totalWeight = 0;
		totalWeight += mean ? meanWeight : 0;
		totalWeight += median ? medianWeight : 0;
		totalWeight += mode ? modeWeight : 0;
		totalWeight += range ? rangeWeight : 0;

		if (approximatelyEqual(totalWeight, 1f, .99f)) {
			throw new IllegalArgumentException("The weights of the selected measures do not add up to one.");
		}
	}

	private boolean approximatelyEqual(float val1, float val2, float tolerancePercentage) {
		float diff = Math.abs(val1 - val2);
		float tolerance = tolerancePercentage * val1;
		return diff < tolerance;
	}

}
