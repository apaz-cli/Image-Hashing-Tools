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

	// @nof
	//	default ImageHash hash(IImage<?> img) {
	//		// You can should usually create non-anonymous class with a constructor that will make
	//		// IHashAlgorithms with various sizes or other parameters.
	//		
	//		// Everything is up to you, although it's recommended to convert to one type of image.
	// 		// Also worth noting is that for some reason it's generally faster to resize before converting.
	//		// This holds true when what you're resizing to is smaller in memory than what you had originally.
	//
	//		// Resize to some thumbnail of the original image.
	//		int width = 8;
	//  	int[] pixels = PixelUtils.byteArrayToInt(img.resizeBilinear(width, width).toGreyscale().getPixels());
	//		
	//      // Declare a long array. Make it as big as you need, but keep track of how many bits of this array you use.
	//		// 
	//		long[] hashBits = new long[64];
	// 
	//      // Now do the actual calculations for the hash, and pack the results into the hashBits array. Use all the space.
	//  	
	//		// Finally, return an ImageHash as shown below. If img is a SourcedImage, then this will return a source. 
	//		// If SourcedImages are nested, then this gets the topmost source.
	//	  	return new ImageHash(this, hashBits, this.findSource(img));
	//	}
	// @dof

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

	// nameLength(ComparisonType)
	default String getHashInformation() {
		return new StringBuilder()
				.append(this.getHashName()).append(",")
				.append(this.getHashLength()).append(',')
				.append(this.getComparisonType()).toString();
	}

	default ImageHash hash(IImage<?> img, String source) {
		return this.hash(new SourcedImage(img, source));
	}

	default ImageHash hash(BufferedImage img, String source) {
		return this.hash(new SourcedImage(img, source));
	}

	default ImageHash hash(SourcedImage img) {
		return this.hash((IImage<?>) img);
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
