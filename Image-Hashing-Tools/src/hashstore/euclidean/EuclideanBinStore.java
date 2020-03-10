package hashstore.euclidean;

import java.io.File;
import java.util.List;

import hash.ImageHash;
import hashstore.BaseHashStore;

public class EuclideanBinStore extends BaseHashStore {

	private int[] binShape;

	public EuclideanBinStore(int... shape) {
		this.binShape = shape;
	}

	private int[] binVector(ImageHash h) {
		float[] vec = h.getBitArrayAsFloat();
		return null;
	}

	@Override
	public void accept(ImageHash hash) {
		// TODO Auto-generated method stub
		float[] vector = hash.getBitArrayAsFloat();

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
