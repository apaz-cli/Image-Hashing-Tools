package utils;

import image.implementations.GreyscaleImage;
import image.implementations.YCbCrImage;

public class DiscreteCosineTransforms {
	
	// Does all three channels
	public static YCbCrImage DCTII32(YCbCrImage img) {
		return new YCbCrImage(DCTII32(img.getY()), DCTII32(img.getCb()), DCTII32(img.getCr()));
	}
	
	// Does all three channels
	public static YCbCrImage IDCTII32(YCbCrImage img) {
		return new YCbCrImage(IDCTII32(img.getY()), IDCTII32(img.getCb()), IDCTII32(img.getCr()));
	}
	
	// Expects size 32x32
	public static GreyscaleImage DCTII32(GreyscaleImage img) throws IllegalArgumentException {
		// TODO Write
		return null;
	}
	
	// Expects size 32x32
	public static GreyscaleImage IDCTII32(GreyscaleImage dct) throws IllegalArgumentException {
		// TODO Write
		return null;
	}
	
	
	// Will take any size image, and pad zeroes
	public static GreyscaleImage DCTII8(GreyscaleImage img) {
		// TODO Write
		return null;
	}
	
	// WIll take any size DCT, and pad zeroes
	public static GreyscaleImage IDCTII8(GreyscaleImage dct) {
		// TODO Write
		return null;
	}
}
