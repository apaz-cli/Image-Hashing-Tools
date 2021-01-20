package hash;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import image.IImage;
import image.implementations.SourcedImage;
import utils.ImageUtils;

public interface IHashAlgorithm {

	// Implementing a new hash algorithm is not particularly straightforward.

	// When you implement this interface, create a static block and call
	// AlgLoader.register(new MyHashAlgorithm());
	// Then, before you load one of them from a file, you must load your class.
	// Otherwise, ClassNotFoundException will be thrown from ImageHash#fromString.
	
	// Also remember to override equals() with algEquals() and to implement hashCode().

	// Used for writing results of hash to file.
	abstract String algName();

	abstract int getHashLength();

	abstract ComparisonType getComparisonType();

	// Use ║ as a delimiter, because it's hella invalid in url names and rare in
	// files.
	abstract String toArguments();

	// Split on ║
	abstract IHashAlgorithm fromArguments(String arguments);

	abstract double distance(ImageHash h1, ImageHash h2);

	abstract boolean algEquals(IHashAlgorithm o);

	default boolean canCompare(ImageHash hash1, ImageHash hash2) {
		return this.algEquals(hash1.getAlgorithm()) && this.algEquals(hash2.getAlgorithm());
	}

	abstract boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode);

	// Implementations must all deal with every type of IImage, whether it has an
	// alpha channel or not, or is a SourcedImage.
	abstract ImageHash hash(IImage<?> img);

	// Convert to the desired type and then call the other hash method
	abstract ImageHash hash(BufferedImage img);

	abstract void setMatchMode(MatchMode mode);

	abstract MatchMode getMatchMode();

	/*************/
	/* Overloads */
	/*************/
	default double distance(IImage<?> img1, IImage<?> img2) {
		return this.distance(this.hash(img1), this.hash(img2));
	}

	default ImageHash hash(IImage<?> img, URL source) {
		return this.hash(new SourcedImage(img, source));
	}

	default ImageHash hash(IImage<?> img, File source) {
		return this.hash(new SourcedImage(img, source));
	}

	default ImageHash hash(SourcedImage img) {
		return this.hash((IImage<?>) img);
	}

	default ImageHash hash(BufferedImage img, URL source) {
		return this.hash(new SourcedImage(img, source));
	}

	default ImageHash hash(BufferedImage img, File source) {
		return this.hash(new SourcedImage(img, source));
	}

	default ImageHash hash(File imgFile) throws IOException {
		SourcedImage img = ImageUtils.openImageSourced(imgFile);
		if (img == null) throw new FileNotFoundException();
		return this.hash(img);
	}

	default ImageHash hash(URL imgURL) throws IOException {
		return this.hash(ImageUtils.openImageSourced(imgURL));
	}

	default boolean matches(ImageHash hash1, ImageHash hash2) {
		return this.matches(hash1, hash2, this.getMatchMode());
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
