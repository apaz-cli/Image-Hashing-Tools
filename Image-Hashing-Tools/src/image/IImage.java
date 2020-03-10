package image;

import java.awt.Point;
import java.awt.image.BufferedImage;

import attack.IAttack;
import attack.convolutions.ConvolutionKernel;
import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import image.implementations.RGBImage;
import image.implementations.YCbCrImage;

public interface IImage<T extends IImage<T>> {

	// Additionally, classes implementing IImage should implement constructors
	// taking each of the following.

	// int width, height
	// byte arrays, width, height
	// 2d byte arrays, width, height
	// BufferedImage
	// File, URL (implemented with this(ImageIO.read(img)))

	// Should also include set/getPixel/getPixels methods

	abstract public int getWidth();

	abstract public int getHeight();

	abstract public GreyscaleImage[] getChannels();

	abstract public T deepClone();

	// Resizes also modify self then returns self.
	abstract public T resizeNearest(int width, int height);

	abstract public T rescaleNearest(float widthFactor, float heightFactor);

	abstract public T resizeBilinear(int width, int height);

	abstract public T rescaleBilinear(float widthFactor, float heightFactor);

	abstract public BufferedImage toBufferedImage();

	abstract public GreyscaleImage toGreyscale();

	abstract public RGBImage toRGB();

	abstract public RGBAImage toRGBA();

	default public YCbCrImage toYCbCr() {
		if (this instanceof YCbCrImage) {
			return (YCbCrImage) this;
		}
		return new YCbCrImage(this.toRGB());
	}

	abstract public T flipHorizontal();

	abstract public T flipVertical();

	abstract public T rotate90CW();

	abstract public T rotate90CCW();

	abstract public T rotate180();

	default public T extractSubimage(Point p1, Point p2) {
		return this.extractSubimage(p1.x, p1.y, p2.x, p2.y);
	}

	abstract public T extractSubimage(int x1, int y1, int x2, int y2);

	default public T emplaceSubimage(T subImage, Point p1, Point p2) {
		return this.emplaceSubimage(subImage, p1.x, p1.y, p2.x, p2.y);
	}

	abstract public T emplaceSubimage(T subImage, int x1, int y1, int x2, int y2);

	default public IImage<?> convolveWith(ConvolutionKernel kernel) {
		@SuppressWarnings("unchecked")
		T self = (T) kernel.applyTo(this);
		return self;
	}
	
	
	default public T apply(IAttack attack) {
		@SuppressWarnings("unchecked")
		T self = (T) attack.applyTo(this);
		return self;
	}

	default public T printDimensions() {
		System.out.println("Width: " + this.getWidth() + " Height: " + this.getHeight());
		@SuppressWarnings("unchecked")
		T self = (T) this;
		return self;
	}

}
