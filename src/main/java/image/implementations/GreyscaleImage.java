package image.implementations;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;

import image.IImage;

public class GreyscaleImage implements IImage<GreyscaleImage> {

	private byte[] pixels;
	private int width;
	private int height;
	
	public GreyscaleImage(int width, int height) {
		this.width = width;
		this.height = height;
		// All arrays in java have default value null for objects and 0 for primitives.
		// Therefore, the default will be all black.
		this.pixels = new byte[width * height];
	}

	// Pixel array is not copied. It becomes the backing array.
	public GreyscaleImage(byte[] pixels, int width, int height) throws IllegalArgumentException {
		if (pixels.length != width * height) {
			throw new IllegalArgumentException();
		}
		this.width = width;
		this.height = height;
		this.pixels = pixels;
	}

	public GreyscaleImage(byte[][] pixels, int width, int height) throws IllegalArgumentException {
		this.width = width;
		this.height = height;

		this.pixels = new byte[width * height];
		for (int y = 0; y < this.height; y++) {
			byte[] currentArray = pixels[y];
			if (currentArray.length != this.width) {
				throw new IllegalArgumentException("All subarrays of pixels must be of length equal to the width.");
			}

			for (int x = 0; x < this.width; x++) {
				this.pixels[y * this.width + x] = currentArray[x];
			}
		}
	}

	public GreyscaleImage(BufferedImage img) {
		Raster raster = img.getRaster();
		byte[] imgPixels = ((DataBufferByte) raster.getDataBuffer()).getData();
		boolean alphaExists = img.getAlphaRaster() != null;

		this.width = raster.getWidth();
		this.height = raster.getHeight();
		this.pixels = new byte[width * height];
		if (alphaExists) {
			for (int pixel = 0, idx = 0; pixel < imgPixels.length; idx++) {
				// 0xff is the mask required to convert byte to int
				pixel++;
				int b = imgPixels[pixel++] & 0xff;
				int g = imgPixels[pixel++] & 0xff;
				int r = imgPixels[pixel++] & 0xff;
				this.pixels[idx] = (byte) Math.round((r + g + b) / 3f);
			}
		} else {
			for (int pixel = 0, idx = 0; pixel < imgPixels.length; idx++) {
				int b = imgPixels[pixel++] & 0xff;
				int g = imgPixels[pixel++] & 0xff;
				int r = imgPixels[pixel++] & 0xff;
				this.pixels[idx] = (byte) Math.round((r + g + b) / 3f);
			}
		}
	}

	public GreyscaleImage(File imgFile) throws IOException {
		this(ImageIO.read(imgFile));
	}

	public GreyscaleImage(URL imgURL) throws IOException {
		this(ImageIO.read(imgURL));
	}

	public int getPixel(int index) throws ArrayIndexOutOfBoundsException {
		return this.pixels[index];
	}

	public int getPixel(int x, int y) throws ArrayIndexOutOfBoundsException {
		return this.pixels[y * this.width + x] & 0xff;
	}

	public void setPixel(int x, int y, int val) throws ArrayIndexOutOfBoundsException {
		this.pixels[y * this.width + x] = (byte) val;
	}

	public void setPixel(int index, int val) throws ArrayIndexOutOfBoundsException {
		this.pixels[index] = (byte) val;
	}

	public byte[] getPixels() {
		return this.pixels;
	}

	public byte[][] get2dPixels() {
		byte[][] pixel2d = new byte[this.width][this.height];
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				pixel2d[x][y] = this.pixels[y * this.width + x];
			}
		}
		return pixel2d;
	}

	public RGBImage recolor(Color c) {
		double redFactor = c.getRed() / 255.0;
		double greenFactor = c.getGreen() / 255.0;
		double blueFactor = c.getBlue() / 255.0;

		byte[] red = new byte[this.pixels.length];
		byte[] green = new byte[this.pixels.length];
		byte[] blue = new byte[this.pixels.length];

		for (int i = 0; i < this.pixels.length; i++) {
			int pixelValue = (this.pixels[i] & 0xff);
			red[i] = (byte) Math.round(pixelValue * redFactor);
			green[i] = (byte) Math.round(pixelValue * greenFactor);
			blue[i] = (byte) Math.round(pixelValue * blueFactor);
		}
		return new RGBImage(red, green, blue, this.width, this.height);
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
	public GreyscaleImage deepClone() {
		return new GreyscaleImage(Arrays.copyOf(this.pixels, this.pixels.length), this.getWidth(), this.getHeight());
	}

	@Override
	public GreyscaleImage resizeNearest(int width, int height) throws ArithmeticException {
		// Packs and unpacks the int. This is okay, the performance cost is marginal.
		return this.rescaleNearest(((float) (this.width - 1)) / width, ((float) (this.height - 1)) / height);
	}

	@Override
	public GreyscaleImage rescaleNearest(float widthFactor, float heightFactor) throws ArithmeticException {
		// Throws when new width or height overflows int.maxvalue
		int newWidth = Math.toIntExact(Math.round(this.width * widthFactor));
		int newHeight = Math.toIntExact(Math.round(this.height * heightFactor));
		byte[] newPixels = new byte[newWidth * newHeight];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				int xSample = (int) (x / widthFactor);
				int ySample = (int) (y / heightFactor);
				newPixels[y * newWidth + x] = this.pixels[ySample * this.width + xSample];
			}
		}

		return new GreyscaleImage(newPixels, newWidth, newHeight);
	}

	@Override
	public GreyscaleImage resizeBilinear(int width, int height) {
		byte[] scaled = new byte[width * height];

		float xRatio = ((float) (this.width - 1)) / width;
		float yRatio = ((float) (this.height - 1)) / height;

		int offset = 0;
		int A, B, C, D, x, y, index, gray;
		float x_diff, y_diff;

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {

				x = (int) (xRatio * j);
				y = (int) (yRatio * i);
				x_diff = (xRatio * j) - x;
				y_diff = (yRatio * i) - y;
				index = y * this.width + x;

				A = pixels[index] & 0xff;
				B = pixels[index + 1] & 0xff;
				C = pixels[index + this.width] & 0xff;
				D = pixels[index + this.width + 1] & 0xff;

				gray = (int) (A * (1 - x_diff) * (1 - y_diff) + B * (x_diff) * (1 - y_diff)
						+ C * (y_diff) * (1 - x_diff) + D * (x_diff * y_diff));

				scaled[offset++] = (byte) gray;
			}
		}
		return new GreyscaleImage(scaled, width, height);
	}

	@Override
	public GreyscaleImage rescaleBilinear(float widthFactor, float heightFactor) {
		return resizeBilinear(Math.round(this.width * widthFactor), Math.round(this.height * heightFactor));
	}

	@Override
	public GreyscaleImage resizeBicubic(int width, int height) {
		return this.rescaleBicubic(((float) (this.width - 1)) / width, ((float) (this.height - 1)) / height);
	}

	@Override
	public GreyscaleImage rescaleBicubic(float widthFactor, float heightFactor) {
		return null;
	}

	@Override
	public String toString() {
		String str = "";
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				String element = "" + (this.pixels[y * width + x] & 0xff);
				if (element.length() == 1) {
					element = "  " + element;
				} else if (element.length() == 2) {
					element = " " + element;
				}
				str += element + (x == this.width - 1 ? "" : " ");
			}
			str += "\n";
		}
		return str.substring(0, str.length() - 1);
	}

	@Override
	public BufferedImage toBufferedImage() {
		BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_GRAY);
		img.setData(Raster.createRaster(img.getSampleModel(), new DataBufferByte(this.pixels, this.pixels.length),
				null));
		return img;
	}

	// Returns self.
	@Override
	public GreyscaleImage toGreyscale() {
		return this;
	}

	// Returns new RGBImage, with each color channel backed by this
	@Override
	public RGBImage toRGB() {
		// The constructor does the cloning.
		return new RGBImage(this, this, this);
	}

	// Returns new RGBAImage, with each color channel backed by this, and a new opaque alpha channel.
	@Override
	public RGBAImage toRGBA() {
		// Zero alpha represents completely transparent, so we must set them all to
		// opaque. 
		byte[] alpha = new byte[this.width * this.height];
		Arrays.fill(alpha, (byte) 255);
		return new RGBAImage(this.toRGB(), new GreyscaleImage(alpha, this.width, this.height));
	}

}
