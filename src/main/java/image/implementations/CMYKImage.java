package image.implementations;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import image.IImage;

public class CMYKImage implements IImage<CMYKImage> {

	// COLORSPACE EQUATIONS
	// Rw = R ÷ 255
	// Gw = G ÷ 255
	// Bw = B ÷ 255

	// K = 1 - max(Rw, Gw, Bw);
	// C = (1 - Rw - K) ÷ (1 - K)
	// M = (1 - Gw - K) ÷ (1 - K)
	// Y = (1 - Bw - K) ÷ ( 1 - K)

	private float[] C;
	private float[] M;
	private float[] Y;
	private float[] K;
	private int width;
	private int height;

	public CMYKImage(float[] C, float[] M, float[] Y, float[] K, int width, int height) {
		this.width = width;
		this.height = height;
		this.C = C;
		this.M = M;
		this.Y = Y;
		this.K = K;
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
		this.K = new float[length];
		this.C = new float[length];
		this.M = new float[length];
		this.Y = new float[length];

		i = 0;
		for (; i < length; i++) {
			this.K[i] = 1 - Math.max(Rw[i], Math.max(Gw[i], Bw[i]));
			this.C[i] = (1 - Rw[i] - this.K[i]) / (1 - this.K[i]);
			this.M[i] = (1 - Gw[i] - this.K[i]) / (1 - this.K[i]);
			this.Y[i] = (1 - Bw[i] - this.K[i]) / (1 - this.K[i]);
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

	@Override
	public CMYKImage deepClone() {
		return new CMYKImage(Arrays.copyOf(this.C, this.C.length), Arrays.copyOf(this.M, this.M.length),
				Arrays.copyOf(this.Y, this.Y.length), Arrays.copyOf(this.K, this.K.length), this.width, this.height);
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

		// r = 255 × ( 1 - C ÷ 100 ) × ( 1 - K ÷ 100 )
		// g = 255 × ( 1 - M ÷ 100 ) × ( 1 - K ÷ 100 )
		// b = 255 × ( 1 - Y ÷ 100 ) × ( 1 - K ÷ 100 )
		int red, green, blue;
		int i = 0;
		for (; i < length; i++) {
			red = Math.round(255f * (1 - C[i] / 100f) * (1 - K[i] / 100f));
			if (red > 255) {
				red = 255;
			} else if (red < 0) {
				red = 0;
			}
			r[i] = (byte) red;

			green = Math.round(255f * (1 - M[i] / 100f) * (1 - K[i] / 100f));
			if (green > 255) {
				green = 255;
			} else if (green < 0) {
				green = 0;
			}
			g[i] = (byte) green;

			blue = Math.round(255f * (1 - Y[i] / 100f) * (1 - K[i] / 100f));
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
