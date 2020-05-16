package image.implementations;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import image.IImage;

public class SourcedImage implements IImage<SourcedImage> {

	private String source;
	private IImage<?> img;
	boolean isURL; // If isn't a URL, assume that means it's a File

	// Master Constructor
	public SourcedImage(BufferedImage img, String source, boolean isURL) {
		this.img = new RGBAImage(img);
		this.source = source;
		this.isURL = isURL;
	}

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

	public SourcedImage(BufferedImage img, URL source) {
		this.img = new RGBAImage(img);
		this.source = source == null ? null : source.toString();
		this.isURL = true;
	}

	public SourcedImage(BufferedImage img, File source) {
		this.img = new RGBAImage(img);
		this.source = source == null ? null : source.toString();
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
		this.source = source == null ? null : source.toString();
		this.isURL = true;
	}

	public SourcedImage(IImage<?> img, File source) {
		this.img = img;
		this.source = source == null ? null : source.toString();
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
		// Does not unwrap it completely. If this SourcedImage is backed by another,
		// this will return the SourcedImage.
		return this.img;
	}

	public BufferedImage unwrapBufferedImage() {
		return this.img.toBufferedImage();
	}

	public String getSource() {
		return this.source;
	}

	@Override
	public SourcedImage deepClone() {
		return new SourcedImage(this.img.deepClone(), this.source, this.isURL);
	}

	@Override
	public int getWidth() {
		return this.img.getWidth();
	}

	@Override
	public int getHeight() {
		return this.img.getHeight();
	}

	@Override
	public GreyscaleImage[] getChannels() {
		return this.img.getChannels();
	}

	@Override
	public SourcedImage resizeNearest(int width, int height) {
		this.img = this.img.resizeNearest(width, height);
		return this;
	}

	@Override
	public SourcedImage rescaleNearest(float widthFactor, float heightFactor) {
		this.img = this.img.rescaleNearest(widthFactor, heightFactor);
		return this;
	}

	@Override
	public SourcedImage resizeBilinear(int width, int height) {
		this.img = this.img.resizeBilinear(width, height);
		return this;
	}

	@Override
	public SourcedImage rescaleBilinear(float widthFactor, float heightFactor) {
		this.img = this.img.rescaleBilinear(widthFactor, heightFactor);
		return this;
	}

	@Override
	public BufferedImage toBufferedImage() {
		return this.img.toBufferedImage();
	}

	@Override
	public GreyscaleImage toGreyscale() {
		return this.img.toGreyscale();
	}

	@Override
	public RGBImage toRGB() {
		return this.img.toRGB();
	}

	@Override
	public RGBAImage toRGBA() {
		return this.img.toRGBA();
	}

	@Override
	public SourcedImage flipHorizontal() {
		this.img = this.img.flipHorizontal();
		return this;
	}

	@Override
	public SourcedImage flipVertical() {
		this.img = this.img.flipVertical();
		return this;
	}

	@Override
	public SourcedImage rotate90CW() {
		this.img = this.img.rotate90CCW();
		return this;
	}

	@Override
	public SourcedImage rotate90CCW() {
		this.img = this.img.rotate90CCW();
		return this;
	}

	@Override
	public SourcedImage rotate180() {
		this.img = this.img.rotate180();
		return this;
	}

	@Override
	public SourcedImage extractSubimage(int x1, int y1, int x2, int y2) {
		this.img = this.img.extractSubimage(x1, y1, x2, y2);
		return this;
	}

	@Override
	public SourcedImage emplaceSubimage(SourcedImage subImage, int x1, int y1, int x2, int y2) {

		if (this.img instanceof SourcedImage) {
			this.img = ((SourcedImage) this.img).emplaceSubimage(subImage, x1, y1, x2, y2);
			return this;
		}

		SourcedImage emplaced = null;
		try {
			this.img = this.img.getClass().getConstructor(IImage.class).newInstance(subImage.unwrap());
			return this;
		} catch (Exception e) {
			System.err.println("THERE HAS BEEN A PROBLEM, AND THIS METHOD HAS BROKEN. "
					+ "PLEASE CREATE AN ISSUE ON GITHUB WITH THE FOLLOWING STACK TRACE: ");
			e.printStackTrace();
			System.exit(2);
		}
		return emplaced;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof SourcedImage
				? this.source == ((SourcedImage) o).getSource() && ((SourcedImage) o).unwrap() == this.img
				: false;
	}

}
