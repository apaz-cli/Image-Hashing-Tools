package image.implementations;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;

import image.IImage;

public class HSIImage implements IImage<HSIImage> {

	private float[] h; // Hue
	private float[] s; // Saturation
	private float[] i; // Intensity
	private int width;
	private int height;

	public HSIImage(int width, int height) {

	}
	
	public HSIImage(RGBImage img) {
		
	}

	public HSIImage(BufferedImage img) {

	}

	public HSIImage(File imgFile) throws IOException {
		this(ImageIO.read(imgFile));
	}

	public HSIImage(URL imgURL) throws IOException {
		this(ImageIO.read(imgURL));
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}
	
	public float[] getH() {
		return this.h;
	}
	
	public float[] getS() {
		return this.s;
	}
	
	public float[] getI() {
		return this.i;
	}

	@Override
	public HSIImage deepClone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HSIImage resizeNearest(int width, int height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HSIImage rescaleNearest(float widthFactor, float heightFactor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HSIImage resizeBilinear(int width, int height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HSIImage rescaleBilinear(float widthFactor, float heightFactor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedImage toBufferedImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GreyscaleImage toGreyscale() {
		return this.toRGB().toGreyscale();
	}

	@Override
	public RGBImage toRGB() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RGBAImage toRGBA() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof HSIImage)) {
			return false;
		}
		HSIImage other = (HSIImage) obj;
		return Arrays.equals(this.h, other.getH()) && Arrays.equals(this.s, other.getS())
				&& Arrays.equals(this.i, other.getI());
	}

}
