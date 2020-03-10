package hashstore.euclidean;

import java.io.File;
import java.util.List;

import hash.ImageHash;
import hashstore.BaseHashStore;

public class EuclideanHyperplaneStore extends BaseHashStore {
	
	
	private EuclideanVector[] hyperplanes;
	private int[] shape;
	
	public int[] getShape() {
		return this.shape;
	}
	
	
	
	@Override
	public void accept(ImageHash hash) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public File getRootFolder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageHash findNearest(ImageHash h) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(ImageHash h) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<ImageHash> findInRadius(ImageHash h) {
		// TODO Auto-generated method stub
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
