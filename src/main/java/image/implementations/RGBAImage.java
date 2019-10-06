package image.implementations;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

import image.IImage;

public class RGBAImage implements IImage<RGBAImage> {

	private int width;
	private int height;
	private RGBImage rgb;
	private GreyscaleImage a;

	public RGBAImage(int width, int height) {
		this.width = width;
		this.height = height;
		this.rgb = new RGBImage(width, height);
		this.a = new GreyscaleImage(width, height);
	}

	public RGBAImage(RGBImage rgb, GreyscaleImage alpha) throws IllegalArgumentException {
		if (rgb.getWidth() != alpha.getWidth() || rgb.getHeight() != alpha.getHeight()) {
			throw new IllegalArgumentException(
					"The widths and heights of all the images must be the same to construct an RGBAImage.");
		}
		this.rgb = rgb.deepClone();
		this.a = alpha.deepClone();
	}

	public RGBAImage(RGBImage rgb, byte[] alpha) throws IllegalArgumentException {
		if (alpha.length != rgb.getWidth() * rgb.getHeight()) {
			throw new IllegalArgumentException(
					"The a array's length must be equal to rgb.getWidth() * rgb.getHeight().");
		}

		this.width = rgb.getWidth();
		this.height = rgb.getHeight();
		this.rgb = rgb.deepClone();
		this.a = new GreyscaleImage(alpha, this.width, this.height);
	}

	public RGBAImage(GreyscaleImage r, GreyscaleImage g, GreyscaleImage b, GreyscaleImage alpha)
			throws IllegalArgumentException {
		this.width = r.getWidth();
		this.height = r.getHeight();
		if (g.getWidth() != this.width || b.getWidth() != this.width || alpha.getWidth() != this.width
				|| g.getHeight() != this.height || b.getHeight() != this.height || alpha.getHeight() != this.height) {
			throw new IllegalArgumentException(
					"The widths and heights of all the images must be the same to construct an RGBAImage.");
		}

		// R, G, B cloned in constructor
		this.rgb = new RGBImage(r, g, b);
		this.a = alpha.deepClone();
	}

	public RGBAImage(BufferedImage img) {
		Raster raster = img.getRaster();
		byte[] imgPixels = ((DataBufferByte) raster.getDataBuffer()).getData();
		boolean alphaExists = img.getAlphaRaster() != null;

		this.width = raster.getWidth();
		this.height = raster.getHeight();
		byte[] red = new byte[width * height];
		byte[] green = new byte[width * height];
		byte[] blue = new byte[width * height];
		byte[] alpha = new byte[width * height];

		if (alphaExists) {
			for (int pixel = 0, idx = 0; pixel < imgPixels.length; idx++) {
				// 0xFF is the mask required to convert byte to int
				alpha[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				blue[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				green[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				red[idx] = (byte) (imgPixels[pixel++] & 0xFF);
			}
		} else {
			for (int idx = 0; idx < alpha.length; idx++) {
				alpha[idx] = (byte) 255;
			}

			for (int pixel = 0, idx = 0; pixel < imgPixels.length; idx++) {
				blue[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				green[idx] = (byte) (imgPixels[pixel++] & 0xFF);
				red[idx] = (byte) (imgPixels[pixel++] & 0xFF);
			}
		}

		this.rgb = new RGBImage(red, green, blue, this.width, this.height);
		this.a = new GreyscaleImage(alpha, this.width, this.height);
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	public RGBImage getRGB() {
		return this.rgb;
	}

	public GreyscaleImage getRed() {
		return this.rgb.getRed();
	}

	public GreyscaleImage getGreen() {
		return this.rgb.getGreen();
	}

	public GreyscaleImage getBlue() {
		return this.rgb.getBlue();
	}

	public GreyscaleImage getAlpha() {
		return this.a;
	}

	@Override
	public RGBAImage deepClone() {
		return new RGBAImage(this.rgb.deepClone(), this.a.deepClone());
	}

	@Override
	public RGBAImage resizeNearest(int width, int height) {
		return new RGBAImage(this.rgb.resizeNearest(width, height), this.a.resizeNearest(width, height));
	}

	@Override
	public RGBAImage rescaleNearest(float widthFactor, float heightFactor) {
		return new RGBAImage(this.rgb.rescaleNearest(widthFactor, heightFactor),
				this.a.rescaleNearest(widthFactor, heightFactor));
	}

	@Override
	public RGBAImage resizeBilinear(int width, int height) {
		return new RGBAImage(this.rgb.resizeBilinear(width, height), this.a.resizeBilinear(width, height));
	}

	@Override
	public RGBAImage rescaleBilinear(float widthFactor, float heightFactor) {
		return new RGBAImage(this.rgb.rescaleBilinear(widthFactor, heightFactor),
				this.a.rescaleBilinear(widthFactor, heightFactor));
	}

	@Override
	public RGBAImage resizeBicubic(int width, int height) {
		return new RGBAImage(this.rgb.resizeBicubic(width, height), this.a.resizeBicubic(width, height));
	}

	@Override
	public RGBAImage rescaleBicubic(float widthFactor, float heightFactor) {
		return new RGBAImage(this.rgb.rescaleBicubic(widthFactor, heightFactor),
				this.a.rescaleBicubic(widthFactor, heightFactor));
	}

	@Override
	public BufferedImage toBufferedImage() {
		byte[] BIPixels = new byte[this.width * this.height * 4];

		byte[] apixels = this.a.getPixels();
		byte[] blue = this.rgb.getBlue().getPixels();
		byte[] green = this.rgb.getGreen().getPixels();
		byte[] red = this.rgb.getRed().getPixels();

		int offset = 0;
		int pos = 0;
		for (;;) {
			BIPixels[offset++] = apixels[pos];
			BIPixels[offset++] = blue[pos];
			BIPixels[offset++] = green[pos];
			BIPixels[offset++] = red[pos];

			if (offset == BIPixels.length) {
				break;
			}
			pos++;
		}

		BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_4BYTE_ABGR);
		img.setData(Raster.createRaster(img.getSampleModel(), new DataBufferByte(BIPixels, BIPixels.length), null));

		return img;
	}

	// Creates new
	@Override
	public GreyscaleImage toGreyscale() {
		return this.rgb.toGreyscale();
	}

	// Returns backing RGB
	@Override
	public RGBImage toRGB() {
		return this.rgb;
	}

	// Returns self
	@Override
	public RGBAImage toRGBA() {
		return this;
	}

}
