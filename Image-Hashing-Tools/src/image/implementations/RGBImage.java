package image.implementations;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import attack.IAttack;
import image.IImage;
import utils.ImageUtils;

public class RGBImage implements IImage<RGBImage> {

	private GreyscaleImage r;
	private GreyscaleImage g;
	private GreyscaleImage b;
	private int width;
	private int height;

	public RGBImage(int width, int height) {
		this.width = width;
		this.height = height;
		this.r = new GreyscaleImage(width, height);
		this.g = new GreyscaleImage(width, height);
		this.b = new GreyscaleImage(width, height);
	}

	public RGBImage(byte[] red, byte[] green, byte[] blue, int width, int height) throws IllegalArgumentException {
		this.width = width;
		this.height = height;
		// GreyscaleImage constructor will throw IllegalArgumentException if the byte
		// arrays are not all the same, correct size.
		this.r = new GreyscaleImage(red, width, height);
		this.g = new GreyscaleImage(green, width, height);
		this.b = new GreyscaleImage(blue, width, height);
	}

	public RGBImage(byte[][] red, byte[][] green, byte[][] blue, int width, int height)
			throws IllegalArgumentException {
		this.width = width;
		this.height = height;
		// GreyscaleImage constructor will throw IllegalArgumentException if the byte
		// arrays are not all the same, correct size.
		this.r = new GreyscaleImage(red, width, height);
		this.g = new GreyscaleImage(green, width, height);
		this.b = new GreyscaleImage(blue, width, height);
	}

	public RGBImage(IImage<?> img) {
		this.width = img.getWidth();
		this.height = img.getHeight();
		RGBImage rgb = img.toRGB();
		this.r = rgb.getRed();
		this.g = rgb.getGreen();
		this.b = rgb.getBlue();
	}

	// r, g, b, become backing
	public RGBImage(GreyscaleImage red, GreyscaleImage green, GreyscaleImage blue) {
		int width = red.getWidth(), height = red.getHeight();
		if (width != green.getWidth() || width != blue.getWidth() || height != green.getHeight()
				|| height != blue.getHeight()) {
			throw new IllegalArgumentException("All three images have the same dimensions.");
		}

		this.width = red.getWidth();
		this.height = red.getHeight();
		this.r = red;
		this.g = green;
		this.b = blue;
	}

	public RGBImage(GreyscaleImage[] rgb) {
		if (rgb.length != 3) { throw new IllegalArgumentException("Array must contain exactly three color channels."); }
		RGBImage self = new RGBImage(rgb[0], rgb[1], rgb[2]);
		this.width = self.getWidth();
		this.height = self.getHeight();
		this.r = self.getRed();
		this.g = self.getGreen();
		this.b = self.getBlue();
	}

	public RGBImage(BufferedImage img) throws IllegalArgumentException {
		if (img == null) { throw new IllegalArgumentException("Argument cannot be null."); }

		// I have to do some tricks, because Java's image library is garbage for this
		// sort of thing.
		// It's not possible to just safely get the data from any arbitrary
		// BufferedImage, even if DataBuffer instanceof DataBufferByte.
		// Instead, overwrite a new, properly formatted image with the one to parse, let
		// BufferedImage handle it, and try again.

		// This also fixes a Java 8 ImageIO bug. When you're opening animated gifs
		// without this, it would usually explode. Now however, it

		// Draw img onto new image with known encoding.
		BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		newImg.createGraphics().drawImage(img, 0, 0, null);
		Raster newRaster = newImg.getRaster();

		// Get pixel data
		byte[] imgPixels = null;
		DataBuffer newBuffer = newImg.getRaster().getDataBuffer();
		if (newBuffer instanceof DataBufferByte) {
			imgPixels = ((DataBufferByte) newBuffer).getData();
		} else {
			throw new IllegalArgumentException("Something has gone terribly wrong, this should never happen");
		}

		// Separate R, G, B, the data for this object.
		boolean alphaExists = newImg.getAlphaRaster() != null;
		this.width = newRaster.getWidth();
		this.height = newRaster.getHeight();
		byte[] red = new byte[width * height];
		byte[] green = new byte[width * height];
		byte[] blue = new byte[width * height];

		if (alphaExists) {
			for (int pixel = 0, idx = 0; pixel < imgPixels.length; idx++) {
				pixel++;
				blue[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				green[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				red[idx] = (byte) (imgPixels[pixel++] & 0xFF);
			}
		} else {
			for (int pixel = 0, idx = 0; pixel < imgPixels.length; idx++) {
				blue[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				green[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				red[idx] = (byte) (imgPixels[pixel++] & 0xFF);
			}
		}

		this.r = new GreyscaleImage(red, width, height);
		this.g = new GreyscaleImage(green, width, height);
		this.b = new GreyscaleImage(blue, width, height);

	}

	public RGBImage(File imgFile) throws IOException {
		this(ImageUtils.openImage(imgFile));
	}

	public RGBImage(URL imgURL) throws IOException {
		this(ImageUtils.openImage(imgURL));
	}

	public GreyscaleImage getRed() { return r; }

	public GreyscaleImage getGreen() { return g; }

	public GreyscaleImage getBlue() { return b; }

	@Override
	public int getWidth() { return this.width; }

	@Override
	public int getHeight() { return this.height; }

	@Override
	public GreyscaleImage[] getChannels() { return new GreyscaleImage[] { this.r, this.g, this.b }; }

	@Override
	public RGBImage deepClone() {
		return new RGBImage(r.deepClone(), g.deepClone(), b.deepClone());
	}

	@Override
	public RGBImage resizeNearest(int width, int height) {
		return new RGBImage(r.resizeNearest(width, height), g.resizeNearest(width, height),
				b.resizeNearest(width, height));
	}

	@Override
	public RGBImage resizeBilinear(int width, int height) {
		return new RGBImage(r.resizeBilinear(width, height), g.resizeBilinear(width, height),
				b.resizeBilinear(width, height));
	}

	@Override
	public RGBImage rescaleNearest(float widthFactor, float heightFactor) {
		return new RGBImage(r.rescaleNearest(widthFactor, heightFactor), g.rescaleNearest(widthFactor, heightFactor),
				b.rescaleNearest(widthFactor, heightFactor));
	}

	@Override
	public RGBImage rescaleBilinear(float widthFactor, float heightFactor) {
		return new RGBImage(r.rescaleBilinear(widthFactor, heightFactor), g.rescaleBilinear(widthFactor, heightFactor),
				b.rescaleBilinear(widthFactor, heightFactor));
	}

	@Override
	public BufferedImage toBufferedImage() {
		byte[] BIPixels = new byte[this.width * this.height * 3];

		byte[] blue = b.getPixels();
		byte[] green = g.getPixels();
		byte[] red = r.getPixels();

		int offset = 0;
		int pos = 0;
		for (;;) {
			BIPixels[offset++] = blue[pos];
			BIPixels[offset++] = green[pos];
			BIPixels[offset++] = red[pos];

			if (offset == BIPixels.length) {
				break;
			}
			pos++;
		}

		BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_3BYTE_BGR);
		img.setData(Raster.createRaster(img.getSampleModel(), new DataBufferByte(BIPixels, BIPixels.length), null));

		return img;
	}

	// Creates new out of averages
	@Override
	public GreyscaleImage toGreyscale() {
		byte[] red = this.r.getPixels();
		byte[] green = this.g.getPixels();
		byte[] blue = this.b.getPixels();

		// Parallel average as int
		int[] average = new int[this.width * this.height];
		Arrays.parallelSetAll(average,
				i -> Math.round((((red[i] & 0xff) + (green[i] & 0xff) + (blue[i] & 0xff)) / 3f)));

		// Convert to byte
		byte[] byteAverage = new byte[average.length];
		for (int i = 0; i < average.length; i++) {
			byteAverage[i] = (byte) average[i];
		}
		return new GreyscaleImage(byteAverage, this.width, this.height);
	}

	// Returns self
	@Override
	public RGBImage toRGB() {
		return this;
	}

	// Uses self to back RGBAImage
	@Override
	public RGBAImage toRGBA() {
		// Zero alpha represents completely transparent, so we must set them all to
		// opaque.
		byte[] alpha = new byte[this.width * this.height];
		Arrays.fill(alpha, (byte) 255);
		return new RGBAImage(this, alpha);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RGBImage) {
			RGBImage other = (RGBImage) obj;
			return this.r.equals(other.getRed()) && this.g.equals(other.getGreen()) && this.b.equals(other.getBlue());
		}
		return false;
	}

	@Override
	public RGBImage flipHorizontal() {
		return new RGBImage(this.r.flipHorizontal(), this.g.flipHorizontal(), this.b.flipHorizontal());
	}

	@Override
	public RGBImage flipVertical() {
		return new RGBImage(this.r.flipVertical(), this.g.flipVertical(), this.b.flipVertical());
	}

	@Override
	public RGBImage rotate90CW() {
		return new RGBImage(this.r.rotate90CW(), this.g.rotate90CW(), this.b.rotate90CW());
	}

	@Override
	public RGBImage rotate90CCW() {
		return new RGBImage(this.r.rotate90CCW(), this.g.rotate90CCW(), this.b.rotate90CCW());
	}

	@Override
	public RGBImage rotate180() {
		return new RGBImage(this.r.rotate180(), this.g.rotate180(), this.b.rotate180());
	}

	@Override
	public RGBImage extractSubimage(int x1, int y1, int x2, int y2) {
		return new RGBImage(this.r.extractSubimage(x1, y1, x2, y2), this.g.extractSubimage(x1, y1, x2, y2),
				this.b.extractSubimage(x1, y1, x2, y2));
	}

	@Override
	public RGBImage emplaceSubimage(RGBImage subImage, int x1, int y1, int x2, int y2) {
		return new RGBImage(this.r.emplaceSubimage(subImage.getRed(), x1, y1, x2, y2),
				this.g.emplaceSubimage(subImage.getGreen(), x1, y1, x2, y2),
				this.b.emplaceSubimage(subImage.getBlue(), x1, y1, x2, y2));
	}

	@Override
	public RGBImage apply(IAttack<RGBImage> attack) {
		return new RGBImage(attack.applyToChannel(this.r), attack.applyToChannel(this.g),
				attack.applyToChannel(this.b));
	}

	@Override
	public boolean hasAlpha() {
		return false;
	}

}
