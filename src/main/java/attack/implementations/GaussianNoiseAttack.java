package attack.implementations;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import attack.IAttack;
import image.IImage;
import image.implementations.*;
import utils.ImageUtils;

public class GaussianNoiseAttack implements IAttack {

	public GreyscaleImage attackGreyscale(GreyscaleImage img, int mean, int sd) {
		byte[] oldPixels = img.getPixels();
		byte[] newPixels = Arrays.copyOf(oldPixels, oldPixels.length);
		int[] gNoise = ImageUtils.gaussianNoise(newPixels.length, mean, sd);

		// Add noise, then correct for over/underflow
		for (int i = 0; i < oldPixels.length; i++) {
			int newPixel = (newPixels[i] & 0xff) + gNoise[i];
			if (newPixel < 0) {
				newPixel = 0;
			} else if (newPixel > 255) {
				newPixel = 255;
			}
			newPixels[i] = (byte) newPixel;
		}

		return new GreyscaleImage(newPixels, img.getWidth(), img.getHeight());
	}

	// default mean = 0, sd = 3
	@Override
	public IImage<?> attack(IImage<?> img) throws UnsupportedOperationException {
		return this.attack(img, 0, 3);
	}

	public IImage<?> attack(IImage<?> img, int mean, int sd) throws UnsupportedOperationException {
		if (img instanceof GreyscaleImage) {
			return this.attackGreyscale((GreyscaleImage) img, mean, sd);
		} else if (img instanceof RGBAImage) {
			RGBAImage rgba = img.toRGBA();
			// Don't attack alpha channel
			return new RGBAImage(this.attackGreyscale(rgba.getRed(), mean, sd),
					this.attackGreyscale(rgba.getGreen(), mean, sd), this.attackGreyscale(rgba.getBlue(), mean, sd),
					rgba.getAlpha());
		} else if (img instanceof RGBImage) {
			RGBImage rgb = img.toRGB();
			return new RGBImage(this.attackGreyscale(rgb.getRed(), mean, sd),
					this.attackGreyscale(rgb.getGreen(), mean, sd), this.attackGreyscale(rgb.getBlue(), mean, sd));
		} else if (img instanceof YCbCrImage) {
			YCbCrImage ycbcr = img.toYCbCr();
			return new YCbCrImage(this.attackGreyscale(ycbcr.getY(), mean, sd),
					this.attackGreyscale(ycbcr.getCb(), mean, sd), this.attackGreyscale(ycbcr.getCr(), mean, sd));
		}
		return this.attack(img.toRGBA());
	}

	@Override
	public IImage<?> attack(BufferedImage img) {
		return this.attack(new RGBAImage(img));
	}

	public IImage<?> attack(BufferedImage img, int mean, int sd) {
		return this.attack(new RGBAImage(img), mean, sd);
	}
}
