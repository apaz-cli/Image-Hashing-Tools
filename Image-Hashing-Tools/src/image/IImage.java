package image;

import java.awt.Point;
import java.awt.image.BufferedImage;

import image.implementations.CMYKImage;
import image.implementations.GreyscaleImage;
import image.implementations.HSIImage;
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

	default public CMYKImage toCMYK() {
		if (this instanceof CMYKImage) {
			return (CMYKImage) this;
		}
		return new CMYKImage(this.toRGB());
	}

	default public HSIImage toHSI() {
		if (this instanceof HSIImage) {
			return (HSIImage) this;
		}
		return new HSIImage(this.toRGB());
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

}
