package attack.convolutions;

import java.util.Arrays;

import image.IImage;
import image.PixelUtils;
import image.implementations.GreyscaleImage;

public class InseperableKernel<T extends IImage<? extends T>> implements ConvolutionKernel<T> {

	private float[] kernel;
	private int sideLength;
	private EdgeMode mode;

	
	
	public InseperableKernel() {
		this(3);
	}

	public InseperableKernel(int sideLength) {
		this(sideLength, EdgeMode.WRAP);
	}

	public InseperableKernel(int sideLength, EdgeMode mode) {
		ConvolutionKernel.assertOdd(sideLength);
		int len = PixelUtils.safeMult(sideLength, sideLength);
		this.kernel = new float[len];
		Arrays.fill(this.kernel, 1f / len);
		this.sideLength = sideLength;
		this.mode = mode;
	}

	public InseperableKernel(float[] kernel, int sideLength) {
		this(kernel, sideLength, EdgeMode.WRAP);
	}

	public InseperableKernel(float[] kernel, int sideLength, EdgeMode mode) {
		ConvolutionKernel.assertOdd(sideLength);
		int len = PixelUtils.safeMult(sideLength, sideLength);
		if (kernel.length != len) {
			throw new IllegalArgumentException(
					"Kernel was not the expected size. Expected: " + len + " got: " + kernel.length);
		}
		this.sideLength = sideLength;
		this.kernel = ConvolutionKernel.normalizeKernel(kernel);
		this.mode = mode;
	}

	public InseperableKernel(float[][] kernel, int sideLength) {
		this(kernel, sideLength, EdgeMode.WRAP);
	}

	public InseperableKernel(float[][] kernel, int sideLength, EdgeMode mode) {
		this(PixelUtils.array2dToArray1d(kernel, sideLength, sideLength), sideLength, mode);

		// Make sure the 2d array looks right
		boolean fail = false;
		if (kernel.length != sideLength) {
			fail = true;
		}
		if (kernel.length > 0) {
			for (int i = 0; i < kernel.length; i++) {
				if (kernel[i].length != sideLength) {
					fail = true;
				}
			}
		} else {
			fail = true;
		}

		if (fail) {
			throw new IllegalArgumentException("The kernel must be a square matrix with an odd side length.");
		}

	}

	@Override
	public GreyscaleImage applyToChannel(GreyscaleImage channel) {
		byte[] channelPixels = channel.getPixels();
		int channelWidth = channel.getWidth(), channelHeight = channel.getHeight();
		int targetPixelX, targetPixelY, pixelValue;
		float kernelWeight, pixelSum;
		int kernelRadius = this.sideLength / 2;

		byte[] newChannel;
		int newWidth = 0, newHeight = 0;

		if (this.mode == EdgeMode.WRAP) {
			newWidth = channelWidth;
			newHeight = channelHeight;
			newChannel = new byte[channelPixels.length];
			// For each pixel in the image that we're convolving
			for (int y = 0; y < channelHeight; y++) {
				for (int x = 0; x < channelWidth; x++) {

					// For each weight in this kernel, apply it to the corresponding pixel and
					pixelSum = 0;
					for (int kernelYdisp = -kernelRadius; kernelYdisp < kernelRadius + 1; kernelYdisp++) {
						for (int kernelXdisp = -kernelRadius; kernelXdisp < kernelRadius + 1; kernelXdisp++) {

							// Get the corresponding pixel, wrapping around if necessary.
							targetPixelX = (x + kernelXdisp);
							if (targetPixelX < 0) {
								targetPixelX += channelWidth;
							} else if (targetPixelX >= channelWidth) {
								targetPixelX -= channelWidth;
							}

							targetPixelY = (y + kernelYdisp);
							if (targetPixelY < 0) {
								targetPixelY += channelHeight;
							} else if (targetPixelY >= channelHeight) {
								targetPixelY -= channelHeight;
							}

							// Apply the weight to the pixel value, and add it. Since the values have
							// already been normalized, there will be no dividing at the end.
							pixelValue = channelPixels[targetPixelY * channelWidth + targetPixelX] & 0xff;
							kernelWeight = this.kernel[(kernelRadius + kernelYdisp) * this.sideLength
									+ (kernelRadius + kernelXdisp)];
							pixelSum += pixelValue * kernelWeight;

						}
					}

					// Adding .5 before casting rounds to nearest.
					newChannel[y * channelWidth + x] = (byte) (pixelSum + .5f);
				}
			}
		} else if (this.mode == EdgeMode.DOWNSIZE) {
			newWidth = channelWidth - (2 * kernelRadius);
			newHeight = channelHeight - (2 * kernelRadius);
			newChannel = new byte[newWidth * newHeight];

			for (int y = kernelRadius; y < channelHeight - kernelRadius; y++) {
				for (int x = kernelRadius; x < channelWidth - kernelRadius; x++) {

					pixelSum = 0;
					for (int kernelYdisp = -kernelRadius; kernelYdisp < kernelRadius+1; kernelYdisp++) {
						for (int kernelXdisp = -kernelRadius; kernelXdisp < kernelRadius+1; kernelXdisp++) {

							// Find pixel in actual image
							targetPixelY = (y + kernelYdisp);
							targetPixelX = (x + kernelXdisp);

							pixelValue = channelPixels[targetPixelY * channelWidth + targetPixelX] & 0xff;
							kernelWeight = this.kernel[(kernelYdisp + kernelRadius) * this.sideLength
									+ (kernelXdisp + kernelRadius)];
							pixelSum += pixelValue * kernelWeight;
						}
					}

					// Adding .5 before casting rounds to nearest.
					newChannel[(y - kernelRadius) * newWidth + (x - kernelRadius)] = (byte) (pixelSum + .5f);
				}
			}
		} else {
			throw new IllegalArgumentException("Unsupported MatchMode.");
		}

		return new GreyscaleImage(newChannel, newWidth, newHeight);
	}

	@Override
	public float[] getKernel() {
		return this.kernel;
	}

	@Override
	public int getSideLength() {
		return this.sideLength;
	}

	@Override
	public EdgeMode getMode() {
		return this.mode;
	}

}
