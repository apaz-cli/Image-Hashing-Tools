package image;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.imageio.ImageIO;

import attack.IAttack;
import hash.IHashAlgorithm;
import hash.ImageHash;
import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import image.implementations.RGBImage;
import image.implementations.SourcedImage;
import image.implementations.YCbCrImage;
import pipeline.operator.ImageOperation;
import utils.DownloadTextFile;

public interface IImage<T extends IImage<? extends T>> {

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

	abstract boolean hasAlpha();

	abstract public GreyscaleImage[] getChannels();

	abstract public T deepClone();

	// Resizes also modify self then returns self.
	abstract public T resizeNearest(int width, int height);

	abstract public T rescaleNearest(float widthFactor, float heightFactor);

	default public T resizeBilinearMaxSideLength(int maxWidthOrHeight) {
		int thisWidth = this.getWidth(), thisHeight = this.getHeight();
		if (thisWidth <= maxWidthOrHeight && thisHeight <= maxWidthOrHeight) {
			@SuppressWarnings("unchecked")
			T self = (T) this;
			return self;
		} else if (thisWidth == thisHeight) {
			return this.resizeBilinear(maxWidthOrHeight, maxWidthOrHeight);
		} else {
			float shrinkFactor = thisWidth > thisHeight ? (((float) maxWidthOrHeight) / thisWidth)
					: (((float) maxWidthOrHeight) / thisHeight);
			return this.rescaleBilinear(shrinkFactor, shrinkFactor);
		}
	}

	abstract public T resizeBilinear(int width, int height);

	abstract public T rescaleBilinear(float widthFactor, float heightFactor);

	abstract public BufferedImage toBufferedImage();

	abstract public GreyscaleImage toGreyscale();

	abstract public RGBImage toRGB();

	abstract public RGBAImage toRGBA();

	default public YCbCrImage toYCbCr() {
		if (this instanceof YCbCrImage) { return (YCbCrImage) this; }
		return new YCbCrImage(this.toRGB());
	}

	default public SourcedImage addSource(URL source) {
		return new SourcedImage(this, source);
	}

	default public SourcedImage addSource(File source) {
		return new SourcedImage(this, source);
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

	abstract T apply(IAttack<T> attack);

	default public T apply(ImageOperation<T> op) {
		@SuppressWarnings("unchecked")
		T self = (T) this;
		return op.apply(self);
	}

	default public T apply(ImageOperation<T> op, int times) {
		@SuppressWarnings("unchecked")
		T self = (T) this;
		for (int i = 0; i < times; i++) {
			self = op.apply(self);
		}
		return self;
	}

	// abstract public T attack(IAttack attack);

	// abstract public T apply(ImageOperation<T> op);

	default public ImageHash hash(IHashAlgorithm algorithm) {
		return algorithm.hash(this);
	}

	default public T printTypeAndDimensions() {
		System.out.println(
				"Type: " + this.getClass().getName() + " Width: " + this.getWidth() + " Height: " + this.getHeight());
		@SuppressWarnings("unchecked")
		T self = (T) this;
		return self;
	}

	default public File save(File f) throws IOException {
		if (f == null) throw new IllegalArgumentException();
		return this.save(f, DownloadTextFile.formatName(f));
	}

	default public File save(File f, String format) throws IOException {
		if (f == null || format == null) throw new IllegalArgumentException();
		if (!DownloadTextFile.formatSupported(format));

		String fname = f.getName();

		if (f.isDirectory()) {
			Random r = new Random();
			int numbers;
			while ((numbers = r.nextInt()) < 0) {}
			fname = "" + numbers;
			this.save(new File(f, fname), format);
		}

		if (f.exists()) {
			ImageIO.write(this.toBufferedImage(), format, f);
			return f;
		}

		String containing = f.getParent();
		containing = containing == null ? "" : containing;

		int extLoc = fname.lastIndexOf('.');
		if (extLoc != -1) fname = fname.substring(0, extLoc);

		return f;
	}

	default public RGBImage imageDiff(IImage<?> img, int resizeToW, int resizeToH) {
		RGBImage img1pix = this.toRGB().resizeBilinear(resizeToW, resizeToH);
		RGBImage img2pix = img.toRGB().resizeBilinear(resizeToW, resizeToH);

		byte[] img1r = img1pix.getRed().getPixels();
		byte[] img1g = img1pix.getGreen().getPixels();
		byte[] img1b = img1pix.getBlue().getPixels();

		byte[] img2r = img2pix.getRed().getPixels();
		byte[] img2g = img2pix.getGreen().getPixels();
		byte[] img2b = img2pix.getBlue().getPixels();

		// diff for each color channel
		byte[] redChannel = new byte[resizeToW * resizeToH];
		byte[] greenChannel = new byte[resizeToW * resizeToH];
		byte[] blueChannel = new byte[resizeToW * resizeToH];

		for (int j = 0; j < resizeToW * resizeToH; j++) {
			redChannel[j] = img1r[j] - img2r[j] >= 0 ? (byte) Math.abs((img1r[j] - img2r[j]) & 0xff)
					: (byte) Math.abs((img2r[j] - img1r[j]) & 0xff);
			greenChannel[j] = img1g[j] - img2g[j] >= 0 ? (byte) Math.abs((img1g[j] - img2g[j]) & 0xff)
					: (byte) Math.abs((img2g[j] - img1g[j]) & 0xff);
			blueChannel[j] = img1b[j] - img2b[j] >= 0 ? (byte) Math.abs((img1b[j] - img2b[j]) & 0xff)
					: (byte) Math.abs((img2b[j] - img1b[j]) & 0xff);
		}

		RGBImage difference = new RGBImage(redChannel, blueChannel, greenChannel, resizeToW, resizeToH);
		return difference;
	}

	default public RGBImage imageDiff(IImage<?> img) {
		return this.imageDiff(img, 512, 512);
	}

}
