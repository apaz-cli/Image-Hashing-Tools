package hash;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import image.IImage;
import image.implementations.SourcedImage;
import utils.ImageUtils;

public interface IHashAlgorithm {

	// Much like with IImageOperation, this must deal with SourcedImages.
	// However, it's much, much simpler. Please see the example below. 
	
	//	default ImageHash hash(IImage<?> img) {
	//		// You can should usually create non-anonymous class with a constructor that will make
	//		// IHashAlgorithms with various sizes or other parameters.
	//		
	//		// Everything is up to you however, although it's recommended to convert to one type of image.
	// 		// Also worth noting is that for some reason it's generally faster to resize before converting.
	//		// This holds true when what you're resizing to is smaller in memory than what you had originally.
	//
	//
	//		int width = 8;
	//  	int[] pixels = PixelUtils.byteArrayToInt(img.resizeBilinear(width, width).toGreyscale().getPixels());
	//		
	//      // Do actual hash on the content of the image, pack it into a long array.
	//		long[] emptyHash = new long[1]
	//  	
	//		// return a new ImageHash this way. If img is a SourcedImage, then this will return a source. 
	//		// If SourcedImages are nested, then this gets the topmost source.
	//	  	return new ImageHash(this, emptyHash, this.findSource(img));
	//	}

	public default String findSource(IImage<?> img) {
		if (img instanceof SourcedImage) {
			return ((SourcedImage) img).getSource();
		}
		return null;
	}

	// Used for writing results of hash to file.
	abstract String getHashName();

	abstract int getHashLength();

	abstract ComparisonType getComparisonType();
	
	abstract String serialize();
	
	abstract IHashAlgorithm deserialize(String serialized);
	
	abstract boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode);

	// Implementations must all deal with SourcedImage.
	abstract ImageHash hash(IImage<?> img);

	// For speed, let the specific algorithm perform its own conversion to
	// its preferred type, rather than providing default implementation. For
	// example, RGBHistogramHash uses RGBImage as its preferred type, whereas
	// AverageHash uses GreyscaleImage. No need to convert to RGBA just to be safe,
	// then to Greyscale.
	abstract ImageHash hash(BufferedImage img);

	default ImageHash hash(IImage<?> img, String source) {
		ImageHash hash = this.hash(img);
		hash.setSource(source);
		return hash;
	}

	default ImageHash hash(BufferedImage img, String source) {
		ImageHash hash = this.hash(img);
		hash.setSource(source);
		return hash;
	}

	default ImageHash hash(SourcedImage img) {
		return this.hash(img.unwrap(), img.getSource());
	}

	default ImageHash hash(File imgFile) throws IOException {
		return this.hash(ImageUtils.openImageSourced(imgFile));
	}

	default ImageHash hash(URL imgURL) throws IOException {
		return this.hash(ImageUtils.openImageSourced(imgURL));
	}
	
	default boolean matches(ImageHash hash1, ImageHash hash2) {
		return this.matches(hash1, hash2, MatchMode.NORMAL);
	}

	default boolean matches(IImage<?> img1, IImage<?> img2) {
		return this.matches(this.hash(img1), this.hash(img2));
	}

	default boolean matches(IImage<?> img1, IImage<?> img2, MatchMode mode) {
		return this.matches(this.hash(img1), this.hash(img2), mode);
	}

	default boolean matches(BufferedImage img1, BufferedImage img2) {
		return this.matches(this.hash(img1), this.hash(img2));
	}

	default boolean matches(BufferedImage img1, BufferedImage img2, MatchMode mode) {
		return this.matches(this.hash(img1), this.hash(img2), mode);
	}

	default boolean matches(SourcedImage img1, SourcedImage img2) {
		return this.matches(this.hash(img1), this.hash(img2));
	}

	default boolean matches(SourcedImage img1, SourcedImage img2, MatchMode mode) {
		return this.matches(this.hash(img1), this.hash(img2), mode);
	}
	
}
