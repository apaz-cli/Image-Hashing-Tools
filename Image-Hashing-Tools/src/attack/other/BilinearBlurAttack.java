package attack.other;

import attack.IAttack;
import image.IImage;
import image.implementations.GreyscaleImage;

public class BilinearBlurAttack implements IAttack {

	@Override
	public GreyscaleImage applyToColorChannel(GreyscaleImage img) {
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

	public IImage<?> attack(IImage<?> img, int iterations)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (iterations < 0) {
			throw new IllegalArgumentException("Number of Iterations cannot be less than zero.");
		}

		IImage<?> copied = img.deepClone();
		for (int i = 0; i < iterations; i++) {
			copied = this.applyTo(copied);
		}
		return copied;
	}

}
