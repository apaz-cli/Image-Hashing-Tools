package hashstore;

import java.io.File;
import java.util.List;

import hash.ImageHash;

public class LinearStore extends BaseHashStore {

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

	@Override
	public void accept(ImageHash hash) {
		// TODO Auto-generated method stub

	}

	@Override
	public File getRootFolder() {
		// TODO Auto-generated method stub
		return null;
	}

}
