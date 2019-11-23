package hash.implementations;

import java.awt.image.BufferedImage;
import java.util.BitSet;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.implementations.GreyscaleImage;
import utils.BenchmarkRunner;
import utils.ImageUtils;

public class DifferenceHash implements IHashAlgorithm {

	@Override
	public String getHashName() {
		return "dHash";
	}

	@Override
	public boolean matches(ImageHash hash1, ImageHash hash2, MatchMode mode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageHash hash(IImage<?> img) {
		// This size seems odd, but we're averaging the pixels next to each other
		// horizontally, and end up with an 8x8 hash.
		img = img.resizeBilinear(9, 8);
		byte[] thumbnail = img.toGreyscale().getPixels();
		int[] averages = new int[64];

		// @nof
		// For every row, average each pixel with the one next to it.
		
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				// Current Row * width of row + position in row
				int index = y * 8 + x;
				
				// Integer division is fine here.
				averages[index] = (
						(thumbnail[index] & 0xff) + 
						(thumbnail[index + 1] & 0xff)
						)/2; 	
			}
		}
		// @dof

		// show average
		byte[] av = new byte[averages.length];
		for (int i = 0; i < averages.length; i++) {
			av[i] = (byte) averages[i];
		}
		BenchmarkRunner.showImage(new GreyscaleImage(av, 8, 8).rescaleNearest(25, 25), "Averages");

		BitSet bs = new BitSet(64);
		for (int y = 0; y < 8; y++) {
			for (int x = 1; x < 8; x++) {
				int i = y * 8 + x;
				System.out.println(i);
				if (averages[i-1] < averages[i]) {
					bs.set(i);
				}
			}
		}
		BenchmarkRunner.showImage(ImageUtils.imageRepresentation(bs).rescaleNearest(25, 25));

		// Set each bit of the hash depending on value adjacent

		return null;
	}

	@Override
	public ImageHash hash(BufferedImage img) {
		return hash(new GreyscaleImage(img));
	}

}
