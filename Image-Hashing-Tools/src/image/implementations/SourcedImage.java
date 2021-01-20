package image.implementations;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import attack.IAttack;
import image.IImage;
import image.PixelUtils;
import utils.ImageUtils;

public class SourcedImage implements IImage<SourcedImage> {

	private String source;
	private IImage<?> img;
	boolean isURL; // If isn't a URL, assume that means it's a File

	// Master Constructor
	public SourcedImage(IImage<?> img, String source, boolean isURL) {
		PixelUtils.assertNotNull(img, source);
		this.img = img instanceof SourcedImage ? ((SourcedImage) img).img : img;
		this.source = source.trim();
		this.isURL = isURL;
	}

	public SourcedImage(IImage<?> img, URL source) { this(img, source == null ? null : source.toString(), true); }

	public SourcedImage(IImage<?> img, File source) { this(img, source == null ? null : source.toString(), false); }

	private SourcedImage(BufferedImage img, String source, boolean isURL) {
		this(img == null ? null : new RGBAImage(img), source, isURL);
	}

	public SourcedImage(BufferedImage img, URL source) { this(img, source == null ? null : source.toString(), true); }

	public SourcedImage(BufferedImage img, File source) { this(img, source == null ? null : source.toString(), false); }

	public SourcedImage(URL url) throws IOException {
		if (url == null) throw new IllegalArgumentException("URL is null.");
		SourcedImage self = ImageUtils.openImageSourced(url);
		if (self == null) throw new IOException();

		this.img = self.img;
		this.source = self.source;
		this.isURL = self.isURL;
	}

	public SourcedImage(File file) throws IOException {
		if (file == null) throw new IllegalArgumentException("URL is null.");
		SourcedImage self = ImageUtils.openImageSourced(file);
		if (self == null) throw new IOException();
		this.img = self.img;
		this.source = self.source;
		this.isURL = self.isURL;
	}

	// Returns true if is a url, false if was a file, null if string.
	public boolean sourceIsURL() { return this.isURL; }

	public boolean sourceIsFile() { return !this.isURL; }

	public URL getIfURL() {
		if (this.isURL) try {
			return new URL(source);
		} catch (MalformedURLException e) {
			return null;
		}
		else return null;
	}

	public File getIfFile() {
		if (!this.isURL) {
			return new File(source);
		} else return null;
	}

	public IImage<?> unwrap() { return this.img; }

	public String getSource() { return this.source; }

	// Throws IllegalArgumentException if not from a file, i.e. if this.isFile() is
	// false.
	public File save() throws IOException {
		return ImageUtils.saveImage(this);
	}

	public File save(String format) throws IOException {
		File f = this.getIfFile();
		if (f != null) return this.save(f, format);
		else return null;
	}

	@Override
	public SourcedImage deepClone() { return new SourcedImage(this.img.deepClone(), this.source, this.isURL); }

	@Override
	public int getWidth() { return this.img.getWidth(); }

	@Override
	public int getHeight() { return this.img.getHeight(); }

	@Override
	public GreyscaleImage[] getChannels() { return this.img.getChannels(); }

	@Override
	public SourcedImage resizeNearest(int width, int height) {
		return new SourcedImage(this.img.resizeNearest(width, height), this.source, this.isURL);
	}

	@Override
	public SourcedImage rescaleNearest(float widthFactor, float heightFactor) {
		return new SourcedImage(this.img.rescaleNearest(widthFactor, heightFactor), this.source, this.isURL);
	}

	@Override
	public SourcedImage resizeBilinear(int width, int height) {
		return new SourcedImage(this.img.resizeBilinear(width, height), this.source, this.isURL);
	}

	@Override
	public SourcedImage rescaleBilinear(float widthFactor, float heightFactor) {
		return new SourcedImage(this.img.rescaleBilinear(widthFactor, heightFactor), this.source, this.isURL);
	}

	@Override
	public BufferedImage toBufferedImage() { return this.img.toBufferedImage(); }

	@Override
	public GreyscaleImage toGreyscale() { return this.img.toGreyscale(); }

	@Override
	public RGBImage toRGB() { return this.img.toRGB(); }

	@Override
	public RGBAImage toRGBA() { return this.img.toRGBA(); }

	@Override
	public SourcedImage flipHorizontal() {
		return new SourcedImage(this.img.flipHorizontal(), this.source, this.isURL);
	}

	@Override
	public SourcedImage flipVertical() { return new SourcedImage(this.img.flipVertical(), this.source, this.isURL); }

	@Override
	public SourcedImage rotate90CW() { return new SourcedImage(this.img.rotate90CW(), this.source, this.isURL); }

	@Override
	public SourcedImage rotate90CCW() { return new SourcedImage(this.img.rotate90CCW(), this.source, this.isURL); }

	@Override
	public SourcedImage rotate180() { return new SourcedImage(this.img.rotate180(), this.source, this.isURL); }

	@Override
	public SourcedImage extractSubimage(int x1, int y1, int x2, int y2) {
		return new SourcedImage(this.img.extractSubimage(x1, y1, x2, y2), this.source, this.isURL);
	}

	// TODO I really, really, really wish that there were an easier answer.
	@Override
	public SourcedImage emplaceSubimage(SourcedImage subImage, int x1, int y1, int x2, int y2) {
		try {
			return new SourcedImage(
					this.img.getClass().getConstructor(new Class[] { IImage.class, String.class, boolean.class })
							.newInstance(subImage.unwrap(), this.source, this.isURL),
					this.source, this.isURL);
		} catch (Exception e) {
			System.err.println("THERE HAS BEEN A PROBLEM, AND THIS METHOD HAS BROKEN. "
					+ "PLEASE CREATE AN ISSUE ON GITHUB WITH THE FOLLOWING STACK TRACE: ");
			e.printStackTrace();
			System.exit(2);
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof SourcedImage
				? this.source == ((SourcedImage) o).getSource() && ((SourcedImage) o).unwrap() == this.img
				: false;
	}

	@Override
	public SourcedImage apply(IAttack<SourcedImage> attack) {
		if (!this.hasAlpha()) {
			RGBImage rgb = this.toRGB();
			return new SourcedImage(new RGBImage(attack.applyToChannel(rgb.getRed()),
					attack.applyToChannel(rgb.getGreen()), attack.applyToChannel(rgb.getBlue())), this.source,
					this.isURL);
		} else {
			RGBAImage rgba = this.toRGBA();
			return new SourcedImage(
					new RGBAImage(attack.applyToChannel(rgba.getRed()), attack.applyToChannel(rgba.getGreen()),
							attack.applyToChannel(rgba.getBlue()), attack.applyToChannel(rgba.getAlpha())),
					this.source, this.isURL);
		}
	}

	@Override
	public boolean hasAlpha() { return this.img.hasAlpha(); }

}
