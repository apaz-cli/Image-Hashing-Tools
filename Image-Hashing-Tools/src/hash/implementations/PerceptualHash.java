package hash.implementations;

import java.awt.image.BufferedImage;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.implementations.YCbCrImage;

public class PerceptualHash implements IHashAlgorithm {

	@Override
	public String getHashName() {
		return "pHash";
	}

	@Override
	public int getHashLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageHash hash(IImage<?> img) {
		img = img.resizeBilinear(32, 32);
		YCbCrImage image = img.toYCbCr();
		
		return null;
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		// TODO Auto-generated method stub
		return null;
	}

}
