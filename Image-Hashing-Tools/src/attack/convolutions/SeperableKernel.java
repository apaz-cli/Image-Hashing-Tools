package attack.convolutions;

import image.IImage;
import image.PixelUtils;
import image.implementations.GreyscaleImage;

public class SeperableKernel<T extends IImage<? extends T>> implements ConvolutionKernel<T> {

	private int sideLength;
	private float[] xKernel;
	private float[] yKernel;
	private EdgeMode mode;

	public SeperableKernel(float[] xKernel, float[] yKernel, EdgeMode mode) {
		this(xKernel, yKernel, xKernel.length, mode);
	}

	public SeperableKernel(float[] xKernel, float[] yKernel, int sideLength, EdgeMode mode) {
		if (xKernel.length != yKernel.length) {
			throw new IllegalArgumentException("x and y kernels are not the same length.");
		}
		PixelUtils.safeSquare(sideLength);

		this.sideLength = sideLength;
		this.xKernel = ConvolutionKernel.normalizeKernel(xKernel);
		this.yKernel = ConvolutionKernel.normalizeKernel(yKernel);
		this.mode = mode;
	}

	@Override
	public float[] getKernel() {
		float[] kernel = new float[this.sideLength * this.sideLength];
		for (int y = 0; y < sideLength; y++) {
			for (int x = 0; x < sideLength; x++) {
				kernel[y * this.sideLength + x] = this.xKernel[x] * this.yKernel[y];
			}
		}
		return kernel;
	}

	@Override
	public int getSideLength() { return this.sideLength; }

	// These apply normalized
	@Override
	public GreyscaleImage applyToChannel(GreyscaleImage channel) {
		return this.attackY(this.attackX(channel));
	}

	public GreyscaleImage attackX(GreyscaleImage channel) {
		byte[] channelPixels = channel.getPixels();
		int channelWidth = channel.getWidth(), channelHeight = channel.getHeight();
		int targetPixelX, pixelValue;
		float kernelWeight, pixelSum;
		int kernelRadius = this.sideLength / 2;
		byte[] newChannel;

		int newWidth = 0, newHeight = 0;

		if (this.mode == EdgeMode.WRAP) {
			newWidth = channelWidth;
			newHeight = channelHeight;
			newChannel = new byte[newWidth * newHeight];

			for (int y = 0; y < channelHeight; y++) {
				for (int x = 0; x < channelWidth; x++) {

					pixelSum = 0;
					for (int kernelXdisp = -kernelRadius; kernelXdisp < kernelRadius + 1; kernelXdisp++) {

						// Find pixel in actual image
						targetPixelX = (x + kernelXdisp);
						// Wrap if necessary, % doesn't mean mod in java.
						if (targetPixelX < 0) {
							targetPixelX += channelWidth;
						} else if (targetPixelX >= channelWidth) {
							targetPixelX -= channelWidth;
						}

						pixelValue = channelPixels[y * channelWidth + targetPixelX] & 0xff;
						kernelWeight = this.xKernel[kernelXdisp + kernelRadius];
						pixelSum += pixelValue * kernelWeight;
					}
					newChannel[y * newWidth + x] = (byte) (pixelSum + .5f);
				}
			}
		} else if (this.mode == EdgeMode.DOWNSIZE) {
			newWidth = channelWidth - (2 * kernelRadius);
			newHeight = channelHeight;
			newChannel = new byte[newWidth * newHeight];

			for (int y = 0; y < channelHeight; y++) {
				for (int x = kernelRadius; x < channelWidth - kernelRadius; x++) {

					pixelSum = 0;
					for (int kernelXdisp = -kernelRadius; kernelXdisp < kernelRadius + 1; kernelXdisp++) {

						// Find pixel in actual image
						targetPixelX = (x + kernelXdisp);

						pixelValue = channelPixels[y * channelWidth + targetPixelX] & 0xff;
						kernelWeight = this.xKernel[kernelXdisp + kernelRadius];
						pixelSum += pixelValue * kernelWeight;
					}
					newChannel[y * newWidth + (x - kernelRadius)] = (byte) (pixelSum + .5f);
				}
			}

		} else {
			throw new IllegalArgumentException("Unsupported MatchMode.");
		}

		return new GreyscaleImage(newChannel, newWidth, newHeight);
	}

	public GreyscaleImage attackY(GreyscaleImage channel) {
		byte[] channelPixels = channel.getPixels();
		int channelWidth = channel.getWidth(), channelHeight = channel.getHeight();
		int targetPixelY, pixelValue;
		float kernelWeight, pixelSum;
		int kernelRadius = this.sideLength / 2;

		byte[] newChannel;
		int newWidth, newHeight;

		if (this.mode == EdgeMode.WRAP) {
			newWidth = channelWidth;
			newHeight = channelHeight;
			newChannel = new byte[newWidth * newHeight];

			for (int y = 0; y < channelHeight; y++) {
				for (int x = 0; x < channelWidth; x++) {

					pixelSum = 0;
					for (int kernelYdisp = -kernelRadius; kernelYdisp < kernelRadius + 1; kernelYdisp++) {

						// Find pixel in actual image
						targetPixelY = (y + kernelYdisp);
						// Wrap if necessary, % doesn't mean mod in java.
						if (targetPixelY < 0) {
							targetPixelY += channelHeight;
						} else if (targetPixelY >= channelHeight) {
							targetPixelY -= channelHeight;
						}

						pixelValue = channelPixels[targetPixelY * channelWidth + x] & 0xff;
						kernelWeight = this.yKernel[kernelYdisp + kernelRadius];
						pixelSum += pixelValue * kernelWeight;
					}
					newChannel[y * newWidth + x] = (byte) (pixelSum + .5f);
				}
			}
		} else if (this.mode == EdgeMode.DOWNSIZE) {
			newWidth = channelWidth;
			newHeight = channelHeight - (2 * kernelRadius);
			newChannel = new byte[newWidth * newHeight];

			for (int y = kernelRadius; y < channelHeight - kernelRadius; y++) {
				for (int x = 0; x < channelWidth; x++) {

					pixelSum = 0;
					for (int kernelYdisp = -kernelRadius; kernelYdisp < kernelRadius + 1; kernelYdisp++) {

						// Find pixel in actual image
						targetPixelY = (y + kernelYdisp);

						pixelValue = channelPixels[targetPixelY * channelWidth + x] & 0xff;
						kernelWeight = this.yKernel[kernelYdisp + kernelRadius];
						pixelSum += pixelValue * kernelWeight;
					}
					newChannel[(y - kernelRadius) * newWidth + x] = (byte) (pixelSum + .5f);
				}
			}
		} else {
			throw new IllegalArgumentException("Unsupported MatchMode.");
		}

		return new GreyscaleImage(newChannel, newWidth, newHeight);
	}

	@Override
	public EdgeMode getMode() { return this.mode; }

	public InseperableKernel<T> toInseperable() {
		return new InseperableKernel<>(this.getKernel(), this.getSideLength(), this.getMode());
	}

}
