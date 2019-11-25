package hash;

import java.awt.image.BufferedImage;

import hash.implementations.*;
import image.IImage;

public class HashFactory {

	// Protect constructor so instances cannot be created
	protected HashFactory() {
	}

	public static ImageHash hash(IImage<?> img, IHashAlgorithm algorithm) {
		return algorithm.hash(img);
	}

	public static ImageHash aHash(IImage<?> img) {
		return new AverageHash().hash(img);
	}

	public static ImageHash aHash(BufferedImage img) {
		return new AverageHash().hash(img);
	}

	public static ImageHash dHash(IImage<?> img) {
		return new DifferenceHash().hash(img);
	}

	public static ImageHash dHash(BufferedImage img) {
		return new DifferenceHash().hash(img);
	}

	public static ImageHash pHash(IImage<?> img) {
		return new PerceptualHash().hash(img);
	}

	public static ImageHash pHash(BufferedImage img) {
		return new PerceptualHash().hash(img);
	}

	public static ImageHash rgbHistHash(IImage<?> img) {
		return new RGBHistogramHash().hash(img);
	}

	public static ImageHash rgbHistHash(BufferedImage img) {
		return new RGBHistogramHash().hash(img);
	}
	
	public static ImageHash greyHistHash(IImage<?> img) {
		return new GreyscaleHistogramHash().hash(img);
	}

	public static ImageHash greyHistHash(BufferedImage img) {
		return new GreyscaleHistogramHash().hash(img);
	}
	
	
}
