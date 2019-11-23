package hash;

import java.awt.image.BufferedImage;

import hash.implementations.*;
import image.IImage;

public class HashFactory {

	// Protect constructor so instances cannot be created
	protected HashFactory() {
	}

	// TODO figure out the correct way to do this
	public static final AverageHash AVERAGE_HASH = new AverageHash();
	public static final PerceptualHash PERCEPTUAL_HASH = new PerceptualHash();
	public static final DifferenceHash DIFFERENCE_HASH = new DifferenceHash();
	public static final GreyscaleHistogramHash GREYSCALE_HISTOGRAM_HASH = new GreyscaleHistogramHash();
	public static final RGBHistogramHash RGB_HISTOGRAM_HASH = new RGBHistogramHash();

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
