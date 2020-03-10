package image.implementations;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import image.IImage;

public class YCbCrImage implements IImage<YCbCrImage> {

	public static GreyscaleImage computeY(BufferedImage img) {
		return computeY(new RGBImage(img));
	}

	public static GreyscaleImage computeY(IImage<?> img) {

		if (img instanceof YCbCrImage) {
			return ((YCbCrImage) img).getY();
		}

		RGBImage image = img.toRGB();
		byte[] red = image.getRed().getPixels();
		byte[] green = image.getGreen().getPixels();
		byte[] blue = image.getBlue().getPixels();

		int width = image.getWidth();
		int height = image.getHeight();

		byte[] y = new byte[width * height];
		float j;
		int r, g, b;

		//@nof
		for (int i = 0; i < red.length; i++) {
			r = (red[i] & 0xff);
			g = (green[i] & 0xff);
			b = (blue[i] & 0xff);
			j = 16 + ((65.738f / 256) * r) 
				   + ((129.057f / 256) * g) 
				   + ((25.064f / 256) * b);
			y[i] = (byte) ((j > 255 ? 
					255f : 
					j < 0 ? 0f : j) + .5f);
		}
		//@dof

		return new GreyscaleImage(y, width, height);
	}

	// Luminance
	private GreyscaleImage Y;
	// Chrominance in the direction of blue
	private GreyscaleImage Cb;
	// Chrominance in the direction of red
	private GreyscaleImage Cr;
	private int width;
	private int height;

	public YCbCrImage(RGBImage rgb) {
		byte[] red = rgb.getRed().getPixels();
		byte[] green = rgb.getGreen().getPixels();
		byte[] blue = rgb.getBlue().getPixels();

		this.width = rgb.getWidth();
		this.height = rgb.getHeight();

		byte[] y = new byte[this.width * this.height];
		byte[] cb = new byte[this.width * this.height];
		byte[] cr = new byte[this.width * this.height];

		// @nof
		int r, g, b;
		float j, k, l;
		for (int i = 0; i < red.length; i++) {
			// Extract workable integer
			r = red[i] & 0xff;
			g = green[i] & 0xff;
			b = blue[i] & 0xff;

			// Convert RGB to YCbCr
			// Equations can be found at https://sistenix.com/rgb2ycbcr.html
			// Specifically at: https://sistenix.com/img/rgb_eq1.svg
			
			j = 16 + ((65.738f / 256) * r) 
				   + ((129.057f / 256) * g) 
			       + ((25.064f / 256) * b);
			// Cb
			k = 128 
					- (r * (37.945f / 256)) 
					- (g * (74.494f / 256)) 
					+ (b * (112.439f / 256));
			// Cr
			l = 128 
					+ (r * (112.439f / 256)) 
					- (g * (94.154f / 256)) 
					- (b * (18.285f / 256));
			
			// Validate
			if (j > 255) {j = 255f;} else if (j < 0) {j = 0f;}
			if (k > 255) {k = 255f;} else if (k < 0) {k = 0f;}
			if (l > 255) {l = 255f;} else if (l < 0) {l = 0f;}
			
			// Round and store values. Casting to byte truncates, 
			// and by adding .5, this is basically an implementation 
			// of Math.round(), that only works for positive numbers.
			// However, as we have validated 0 <= j, k, l <= 255 
			// this is fine.
			y[i] = (byte) (j + .5f);
			cb[i] = (byte) (k + .5f);
			cr[i] = (byte) (l + .5f);
		}
		// @dof

		this.Y = new GreyscaleImage(y, this.width, this.height);
		this.Cb = new GreyscaleImage(cb, this.width, this.height);
		this.Cr = new GreyscaleImage(cr, this.width, this.height);
	}

	// Y, Cb, Cr become backing
	public YCbCrImage(GreyscaleImage Y, GreyscaleImage Cb, GreyscaleImage Cr) throws IllegalArgumentException {
		int len = Y.getPixels().length;
		if (len != Cb.getPixels().length || len != Cr.getPixels().length) {
			throw new IllegalArgumentException("All three images must be the same size.");
		}

		this.Y = Y;
		this.Cb = Cb;
		this.Cr = Cr;
		this.width = Y.getWidth();
		this.height = Y.getHeight();
	}

	public YCbCrImage(BufferedImage img) {
		YCbCrImage ycbcr = new YCbCrImage(new RGBImage(img));
		this.width = ycbcr.getWidth();
		this.height = ycbcr.getHeight();
		this.Y = ycbcr.getY();
		this.Cb = ycbcr.getCb();
		this.Cr = ycbcr.getCr();
	}

	public YCbCrImage(GreyscaleImage[] YCbCr) {
		if (YCbCr.length != 3) {
			throw new IllegalArgumentException("Array must contain exactly three color channels.");
		}
		YCbCrImage self = new YCbCrImage(YCbCr[0], YCbCr[1], YCbCr[2]);
		this.width = self.getWidth();
		this.height = self.getHeight();
		this.Y = self.getY();
		this.Cb = self.getCb();
		this.Cr = self.getCr();
	}

	public YCbCrImage(File imgFile) throws IOException {
		this(ImageIO.read(imgFile));
	}

	public YCbCrImage(URL imgURL) throws IOException {
		this(ImageIO.read(imgURL));
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public GreyscaleImage[] getChannels() {
		return new GreyscaleImage[] { this.Y, this.Cb, this.Cr };
	}

	public GreyscaleImage getY() {
		return this.Y;
	}

	public GreyscaleImage getCb() {
		return this.Cb;
	}

	public GreyscaleImage getCr() {
		return this.Cr;
	}

	@Override
	public YCbCrImage deepClone() {
		return new YCbCrImage(Y.deepClone(), Cb.deepClone(), Cr.deepClone());
	}

	@Override
	public YCbCrImage resizeNearest(int width, int height) {
		return new YCbCrImage(Y.resizeNearest(width, height), Cb.resizeNearest(width, height),
				Cr.resizeNearest(width, height));
	}

	@Override
	public YCbCrImage rescaleNearest(float widthFactor, float heightFactor) {
		return new YCbCrImage(Y.rescaleNearest(widthFactor, heightFactor), Cb.rescaleNearest(widthFactor, heightFactor),
				Cr.rescaleNearest(widthFactor, heightFactor));
	}

	@Override
	public YCbCrImage resizeBilinear(int width, int height) {
		return new YCbCrImage(Y.resizeBilinear(width, height), Cb.resizeBilinear(width, height),
				Cr.resizeBilinear(width, height));
	}

	@Override
	public YCbCrImage rescaleBilinear(float widthFactor, float heightFactor) {
		return new YCbCrImage(Y.rescaleBilinear(widthFactor, heightFactor),
				Cb.rescaleBilinear(widthFactor, heightFactor), Cr.rescaleBilinear(widthFactor, heightFactor));
	}

	@Override
	public BufferedImage toBufferedImage() {
		return this.toRGB().toBufferedImage();
	}

	// Returns the average of the inverse conversion to R, G, B.
	@Override
	public GreyscaleImage toGreyscale() {
		return this.toRGB().toGreyscale();
	}

	// Inverse conversion
	@Override
	public RGBImage toRGB() {
		byte[] luminance = Y.getPixels();
		byte[] chrominanceBlue = Cb.getPixels();
		byte[] chrominanceRed = Cr.getPixels();

		byte[] r = new byte[luminance.length];
		byte[] g = new byte[luminance.length];
		byte[] b = new byte[luminance.length];

		// @nof
		int j, k, l;
		for (int i = 0; i < luminance.length; i++) {
			int y = luminance[i] & 0xff;
			int cb = chrominanceBlue[i] & 0xff;
			int cr = chrominanceRed[i] & 0xff;
			
			j = ((int) (
					298.082f * (y - 16) +
					408.583 * (cr - 128))
					) >> 8;

			k = ((int)(
					298.082 * (y - 16) + 
					-100.291 * (cb - 128) + 
					-208.120 * (cr - 128))
					) >> 8;

			l = ((int) 
					(298.082 * (y - 16) + 
					516.411 * (cb - 128))
					) >> 8;
			
			// Validate arrays
			if (j > 255) {j = 255;} else if (j < 0) {j = 0;}
			if (k > 255) {k = 255;} else if (k < 0) {k = 0;}
			if (l > 255) {l = 255;} else if (l < 0) {l = 0;}
			
			r[i] = (byte) j;
			g[i] = (byte) k;
			b[i] = (byte) l;
			
		}
		// @dof

		return new RGBImage(r, g, b, this.width, this.height);
	}

	// Inverse conversion plus new alpha channel
	@Override
	public RGBAImage toRGBA() {
		byte[] alpha = new byte[this.width * this.height];
		for (int i = 0; i < alpha.length; i++) {
			alpha[i] = (byte) 255;
		}
		return new RGBAImage(this.toRGB(), alpha);
	}

	@Override
	public YCbCrImage flipHorizontal() {
		return new YCbCrImage(Y.flipHorizontal(), Cb.flipHorizontal(), Cr.flipHorizontal());
	}

	@Override
	public YCbCrImage flipVertical() {
		return new YCbCrImage(Y.flipVertical(), Cb.flipVertical(), Cr.flipVertical());
	}

	@Override
	public YCbCrImage rotate90CW() {
		return new YCbCrImage(Y.rotate90CW(), Cb.rotate90CW(), Cr.rotate90CW());
	}

	@Override
	public YCbCrImage rotate90CCW() {
		return new YCbCrImage(Y.rotate90CCW(), Cb.rotate90CCW(), Cr.rotate90CCW());
	}

	@Override
	public YCbCrImage rotate180() {
		return new YCbCrImage(Y.rotate180(), Cb.rotate180(), Cr.rotate180());
	}

	@Override
	public YCbCrImage extractSubimage(int x1, int y1, int x2, int y2) {
		return new YCbCrImage(Y.extractSubimage(x1, y1, x2, y2), Cb.extractSubimage(x1, y1, x2, y2),
				Cr.extractSubimage(x1, y1, x2, y2));
	}

	@Override
	public YCbCrImage emplaceSubimage(YCbCrImage subImage, int x1, int y1, int x2, int y2) {
		return new YCbCrImage(Y.emplaceSubimage(subImage.getY(), x1, y1, x2, y2),
				Cb.emplaceSubimage(subImage.getCb(), x1, y1, x2, y2),
				Cr.emplaceSubimage(subImage.getCr(), x1, y1, x2, y2));
	}

}
