package attack.implementations;

import java.awt.image.BufferedImage;

import attack.IAttack;
import image.IImage;
import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import image.implementations.RGBImage;
import image.implementations.YCbCrImage;

public class BlurAttack implements IAttack {

	public GreyscaleImage attackGreyscale(GreyscaleImage img) {
		int width = img.getWidth(), height = img.getHeight();
		byte[] pixels = img.getPixels();

		byte[] blurred = new byte[width * height];

		float xRatio = ((float) (width - 1)) / width;
		float yRatio = ((float) (height - 1)) / height;

		int offset = 0;
		int A, B, C, D, x, y, index, gray;
		float x_diff, y_diff;

		// @nof
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {

				// To tell the truth I don't really know what's going on here,
				// but it works, so I can't really complain.
				x = (int) (xRatio * j);
				y = (int) (yRatio * i);
				x_diff = (xRatio * j) - x;
				y_diff = (yRatio * i) - y;
				index = y * width + x;

				A = pixels[index] & 0xff;
				B = pixels[index + 1] & 0xff;
				C = pixels[index + width] & 0xff;
				D = pixels[index + width + 1] & 0xff;

				
				gray = (int) (A * (1 - x_diff) * (1 - y_diff) + 
							  B * (x_diff) * (1 - y_diff) + 
							  C * (y_diff) * (1 - x_diff) + 
							  D * (x_diff * y_diff));

				blurred[offset++] = (byte) gray;
			}
		}
		// @dof
		return new GreyscaleImage(blurred, width, height);
	}

	@Override
	public IImage<?> attack(BufferedImage img) {
		return this.attack(new RGBAImage(img));
	}

	public IImage<?> attack(IImage<?> img, int iterations)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (iterations <= 0) {
			throw new IllegalArgumentException("Number of Iterations cannot be less than one.");
		}
		
		IImage<?> copied = img.deepClone();
		for (int i = 0; i < iterations; i++) {
			copied = this.attack(copied);
		}
		return copied;
	}

	@Override
	public IImage<?> attack(IImage<?> img) throws UnsupportedOperationException {
		if (img instanceof GreyscaleImage) {
			return this.attackGreyscale((GreyscaleImage) img);
		} else if (img instanceof RGBAImage) {
			RGBAImage rgba = img.toRGBA();
			// Don't attack alpha channel
			return new RGBAImage(this.attackGreyscale(rgba.getRed()), this.attackGreyscale(rgba.getGreen()),
					this.attackGreyscale(rgba.getBlue()), rgba.getAlpha());
		} else if (img instanceof RGBImage) {
			RGBImage rgb = img.toRGB();
			return new RGBImage(this.attackGreyscale(rgb.getRed()), this.attackGreyscale(rgb.getGreen()),
					this.attackGreyscale(rgb.getBlue()));
		} else if (img instanceof YCbCrImage) {
			YCbCrImage ycbcr = img.toYCbCr();
			return new YCbCrImage(this.attackGreyscale(ycbcr.getY()), this.attackGreyscale(ycbcr.getCb()),
					this.attackGreyscale(ycbcr.getCr()));
		}

		throw new UnsupportedOperationException("Not yet implemented.");
		// return this.attack(img.toRGBA());
	}

}
