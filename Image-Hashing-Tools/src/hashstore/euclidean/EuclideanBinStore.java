package hashstore.euclidean;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hash.ImageHash;
import hashstore.BaseHashStore;
import image.PixelUtils;

public class EuclideanBinStore extends BaseHashStore {

	/*
	 * This is the max value of int, so it isn't a valid index, and it's prime, so
	 * this is a safe number that can't be used as an index by any n-dimensional
	 * array.
	 */
	static int notBinnedBin = 2147483647;

	private int[] shape;
	private int[] center;
	private int[] scales;

	private List<int[]> notBinned = new ArrayList<>();

	private static int[] defaultCenter(int len) {
		int[] center = new int[len];
		Arrays.fill(center, -100);
		return center;
	}

	private static int[] defaultScales(int len) {
		int[] scales = new int[len];
		Arrays.fill(scales, 5);
		return scales;
	}

	public EuclideanBinStore(int... shape) {
		this(shape, defaultCenter(shape.length), defaultScales(shape.length));
	}

	public EuclideanBinStore(int[] shape, int[] center, int[] scales) {
		this.shape = shape;
		this.scales = scales;
		this.center = center;

		try {
			PixelUtils.safeMult(shape);
		} catch (ArithmeticException e) {
			throw new IllegalArgumentException("Product of shape overflows int.");
		}
	}

	private int[] findBin(float[] h) {
		int center = h.length / 2;
		int[] binpos = new int[h.length];
		for (int i = 0; i < h.length; i++) {
			float correctedPosition = h[i] + this.center[i];
			for(;;) {}
		}
		return binpos;
	}

	private int binVector(int[] binPos) {
		return 0;
	}

	@Override
	public void accept(ImageHash hash) {
		// TODO Auto-generated method stub
		float[] vector = hash.getBitArrayAsFloat();
		if () {
			
		}

	}

	@Override
	public ImageHash findNearest(ImageHash h) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(ImageHash h) {

		return false;
	}

	@Override
	public List<ImageHash> findInRadius(ImageHash h) {
		int[] bin = this.binVector(h.getBitArrayAsFloat());

		return null;
	}

	@Override
	public List<ImageHash> toList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> findSourcesInRadius(ImageHash h) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> toSourceList() {
		// TODO Auto-generated method stub
		return null;
	}
}
