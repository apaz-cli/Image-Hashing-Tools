package hash.implementations;

import java.awt.image.BufferedImage;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;

public class GreyscaleHistogramHash implements IHashAlgorithm {

	@Override
	public String getHashName() {
		return "GreyscaleHistogram";
	}

	@Override
	public boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageHash hash(IImage<?> img) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		// TODO Auto-generated method stub
		return null;
	}

}
