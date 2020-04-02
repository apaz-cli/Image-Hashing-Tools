package hashstore.euclidean;

import image.PixelUtils;

public class NdArray<T> {

	protected int[] shape;
	protected T[] data;

	public NdArray(int... shape) {
		this(null, shape);
	}

	@SuppressWarnings("unchecked")
	public NdArray(T[] data, int... shape) {
		if (shape == null) {
			throw new IllegalArgumentException("Shape cannot be null.");
		}
		for (int i : shape) {
			if (i < 1) {
				throw new IllegalArgumentException("Shape cannot contain negative numbers or zero.");
			}
		}

		this.shape = shape;

		int shapeProduct;
		try {
			shapeProduct = PixelUtils.safeMult(shape);
		} catch (ArithmeticException e) {
			throw new IllegalArgumentException("Cannot create NdArray with that shape, because its product overflows int.");
		}
		if (data == null || data.length != shapeProduct) {
			data = (T[]) (new Object[shapeProduct]);
		}
		this.data = data;
	}

	public int index1d(int... indecies) {
		int totalOffset = 1;
		int idx = indecies[0], d = 0;
		if (indecies[0] >= this.shape[0]) {
			throw new ArrayIndexOutOfBoundsException(
					"Dimension " + d + " out of range. Dimension length: " + this.shape[d] + " Got: " + indecies[d]);
		}
		for (d = 1; d < indecies.length; d++) {
			if (indecies[d] >= this.shape[d]) {
				throw new ArrayIndexOutOfBoundsException("Dimension " + d + " out of range. Dimension length: "
						+ this.shape[d] + " Got: " + indecies[d]);
			}
			totalOffset = PixelUtils.safeMult(totalOffset, shape[d - 1]);
			idx = PixelUtils.safeAdd(idx, PixelUtils.safeMult(indecies[d], totalOffset));
		}
		return idx;
	}

	// The same as above, but provided in a static context, allowing for translation
	// This also assumes when you're using the value from it that the shape is valid
	// and applicable.
	public static int index1d(int[] shape, int... indecies) {
		int totalOffset = 1;
		int idx = indecies[0], d = 0;
		if (indecies[0] >= shape[0]) {
			throw new ArrayIndexOutOfBoundsException(
					"Dimension " + d + " out of range. Dimension length: " + shape[d] + " Got: " + indecies[d]);
		}
		for (d = 1; d < indecies.length; d++) {
			if (indecies[d] >= shape[d]) {
				throw new ArrayIndexOutOfBoundsException(
						"Dimension " + d + " out of range. Dimension length: " + shape[d] + " Got: " + indecies[d]);
			}
			totalOffset = PixelUtils.safeMult(totalOffset, shape[d - 1]);
			idx = PixelUtils.safeAdd(idx, PixelUtils.safeMult(indecies[d], totalOffset));
		}
		return idx;
	}

	public T get(int... index) {
		return data[index1d(index)];
	}

	public void set(T val, int... index) throws IndexOutOfBoundsException {
		data[index1d(index)] = val;
	}
}
