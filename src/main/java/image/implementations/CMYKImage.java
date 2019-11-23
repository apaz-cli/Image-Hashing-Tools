package image.implementations;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import image.IImage;

public class CMYKImage implements IImage<CMYKImage> {

	// COLORSPACE EQUATIONS
	// Rw = R ÷ 255
	// Gw = G ÷ 255
	// Bw = B ÷ 255

	// k = 1 - max(Rw, Gw, Bw);
	// c = (1 - Rw - k) ÷ (1 - k)
	// m = (1 - Gw - k) ÷ (1 - k)
	// y = (1 - Bw - k) ÷ ( 1 - k)

	private float[] c;
	private float[] m;
	private float[] y;
	private float[] k;
	private int width;
	private int height;

	public CMYKImage(float[] c, float[] m, float[] y, float[] k, int width, int height) {
		this.width = width;
		this.height = height;
		this.c = c;
		this.m = m;
		this.y = y;
		this.k = k;
	}

	public CMYKImage(RGBImage img) {
		byte[] R = img.getRed().getPixels();
		byte[] G = img.getGreen().getPixels();
		byte[] B = img.getBlue().getPixels();

		this.width = img.getWidth();
		this.height = img.getHeight();
		int length = this.width * this.height;

		// Calculate weights
		float[] Rw = new float[length], Gw = new float[length], Bw = new float[length];
		int i = 0;
		for (; i < length; i++) {
			Rw[i] = (R[i] & 0xff) / 255f;
			Gw[i] = (G[i] & 0xff) / 255f;
			Bw[i] = (B[i] & 0xff) / 255f;
		}

		// Initialize and Calculate KCMY
		this.k = new float[length];
		this.c = new float[length];
		this.m = new float[length];
		this.y = new float[length];

		i = 0;
		for (; i < length; i++) {
			this.k[i] = 1 - Math.max(Rw[i], Math.max(Gw[i], Bw[i]));
			this.c[i] = (1 - Rw[i] - this.k[i]) / (1 - this.k[i]);
			this.m[i] = (1 - Gw[i] - this.k[i]) / (1 - this.k[i]);
			this.y[i] = (1 - Bw[i] - this.k[i]) / (1 - this.k[i]);
		}
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	public float[] getC() {
		return this.c;
	}
	
	public float[] getM() {
		return this.m;
	}
	
	public float[] getY() {
		return this.y;
	}
	
	public float[] getK() {
		return this.k;
	}
	
	@Override
	public CMYKImage deepClone() {
		return new CMYKImage(Arrays.copyOf(this.c, this.c.length), Arrays.copyOf(this.m, this.m.length),
				Arrays.copyOf(this.y, this.y.length), Arrays.copyOf(this.k, this.k.length), this.width, this.height);
	}

	@Override
	public CMYKImage resizeNearest(int width, int height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMYKImage rescaleNearest(float widthFactor, float heightFactor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMYKImage resizeBilinear(int width, int height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMYKImage rescaleBilinear(float widthFactor, float heightFactor) {
		return null;
	}

	@Override
	public BufferedImage toBufferedImage() {
		return this.toRGB().toBufferedImage();
	}

	@Override
	public GreyscaleImage toGreyscale() {
		return this.toRGB().toGreyscale();
	}

	@Override
	public RGBImage toRGB() {
		int length = this.width * this.height;
		byte[] r = new byte[length];
		byte[] g = new byte[length];
		byte[] b = new byte[length];

		// r = 255 × ( 1 - c ÷ 100 ) × ( 1 - k ÷ 100 )
		// g = 255 × ( 1 - m ÷ 100 ) × ( 1 - k ÷ 100 )
		// b = 255 × ( 1 - y ÷ 100 ) × ( 1 - k ÷ 100 )
		int red, green, blue;
		int i = 0;
		for (; i < length; i++) {
			red = Math.round(255f * (1 - c[i] / 100f) * (1 - k[i] / 100f));
			if (red > 255) {
				red = 255;
			} else if (red < 0) {
				red = 0;
			}
			r[i] = (byte) red;

			green = Math.round(255f * (1 - m[i] / 100f) * (1 - k[i] / 100f));
			if (green > 255) {
				green = 255;
			} else if (green < 0) {
				green = 0;
			}
			g[i] = (byte) green;

			blue = Math.round(255f * (1 - y[i] / 100f) * (1 - k[i] / 100f));
			if (blue > 255) {
				blue = 255;
			} else if (blue < 0) {
				blue = 0;
			}
			b[i] = (byte) blue;

		}

		return new RGBImage(r, g, b, this.width, this.height);
	}

	@Override
	public RGBAImage toRGBA() {
		// Zero alpha represents completely transparent, so we must set them all to
		// opaque.
		byte[] alpha = new byte[this.width * this.height];
		Arrays.fill(alpha, (byte) 255);
		return new RGBAImage(this.toRGB(), alpha);
	}

}
