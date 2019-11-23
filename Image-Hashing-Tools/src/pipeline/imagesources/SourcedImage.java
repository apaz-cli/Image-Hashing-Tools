package pipeline.imagesources;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import image.IImage;
import image.implementations.RGBAImage;

public class SourcedImage {

	private String source;
	private IImage<?> img;
	boolean isURL; // If isn't url, is File

	public SourcedImage(BufferedImage img) {
		this(img, (String) null);
	}

	public SourcedImage(IImage<?> img) {
		this(img, (String) null);
	}

	public SourcedImage(BufferedImage img, String source) {
		this.img = new RGBAImage(img);
		this.source = source;
	}

	public SourcedImage(BufferedImage img, String source, boolean isURL) {
		this.img = new RGBAImage(img);
		this.source = source;
		this.isURL = isURL;
	}

	public SourcedImage(BufferedImage img, URL source) {
		this.img = new RGBAImage(img);
		this.source = source.toString();
		this.isURL = true;
	}

	public SourcedImage(BufferedImage img, File source) {
		this.img = new RGBAImage(img);
		this.source = source.toString();
		this.isURL = false;
	}

	public SourcedImage(IImage<?> img, String source) {
		this.img = img;
		this.source = source;
	}

	public SourcedImage(IImage<?> img, String source, boolean isURL) {
		this.img = img;
		this.source = source;
		this.isURL = isURL;
	}

	public SourcedImage(IImage<?> img, URL source) {
		this.img = img;
		this.source = source.toString();
		this.isURL = true;
	}

	public SourcedImage(IImage<?> img, File source) {
		this.img = img;
		this.source = source.toString();
		this.isURL = false;
	}

	// Returns true if is a url, false if was a file, null if string.
	public boolean isURL() {
		return this.isURL;
	}

	public boolean isFile() {
		return !this.isURL;
	}

	public IImage<?> unwrap() {
		return this.img;
	}

	public BufferedImage unwrapBufferedImage() {
		return this.img.toBufferedImage();
	}

	public String getSource() {
		return this.source;
	}

}
