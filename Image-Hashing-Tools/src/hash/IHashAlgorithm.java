package hash;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import image.IImage;
import pipeline.sources.SourcedImage;
import utils.ImageUtils;

public interface IHashAlgorithm {

	// Used for writing results of hash to file.
	abstract String getHashName();

	abstract int getHashLength();

	abstract boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode);

	// Using IImage guarantees that nothing must be changed, even if more IImage
	// implementations are added.
	abstract ImageHash hash(IImage<?> img);

	// For speed, let the specific algorithm perform its own narrowing conversion to
	// its preferred type, rather than providing default implementation. For
	// example, RGBHistogramHash uses RGBImage as its preferred type, whereas
	// AverageHash uses GreyscaleImage.
	abstract ImageHash hash(BufferedImage img);

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

}
