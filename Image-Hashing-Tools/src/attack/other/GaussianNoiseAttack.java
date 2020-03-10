package attack.other;

import java.util.Arrays;

import attack.IAttack;
import image.implementations.*;
import utils.ImageUtils;

public class GaussianNoiseAttack implements IAttack {

	private float mean = 0, sd = 1;

	// default mean = 0, sd = 3
	public GaussianNoiseAttack(float mean, float sd) {
		this.mean = mean;
		this.sd = sd;
	}

	@Override
	public GreyscaleImage applyToColorChannel(GreyscaleImage img) {
		byte[] oldPixels = img.getPixels();
		byte[] newPixels = Arrays.copyOf(oldPixels, oldPixels.length);
		float[] gNoise = ImageUtils.gaussianNoise(newPixels.length, mean, sd);

		// Add noise, then correct for over/underflow
		for (int i = 0; i < oldPixels.length; i++) {
			int newPixel = (newPixels[i] & 0xff) + (int) gNoise[i];
			if (newPixel < 0) {
				newPixel = 0;
			} else if (newPixel > 255) {
				newPixel = 255;
			}
			newPixels[i] = (byte) newPixel;
		}

		return new GreyscaleImage(newPixels, img.getWidth(), img.getHeight());
	}

}
