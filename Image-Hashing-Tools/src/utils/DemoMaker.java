package utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.implementations.DifferenceHash;
import image.IImage;
import image.implementations.GreyscaleImage;
import image.implementations.RGBImage;

public class DemoMaker {

	private static String LennaURL = "https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png";
	private static BufferedImage Lenna = null;
	static {
		try {
			Lenna = ImageUtils.openImage(new URL(LennaURL));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String folderPath = "C:\\Users\\PazderaAaron\\Downloads\\Image Hashing Related\\Examples\\dhash";
		dhashDemo(folderPath);
	}

	private static void dhashDemo(String folderPath) {
		IHashAlgorithm dhash = new DifferenceHash();

		RGBImage rgb = new RGBImage(Lenna);
		// BenchmarkRunner.showImage(rgb);

		RGBImage nineByEight = rgb.resizeBilinear(9, 8);
		nineByEight = nineByEight.rescaleNearest(64, 64);
		// BenchmarkRunner.showImage(nineByEight);

		GreyscaleImage greyNineByEight = nineByEight.toGreyscale();
		greyNineByEight = greyNineByEight.rescaleNearest(64, 64);
		// BenchmarkRunner.showImage(greyNineByEight);

		ImageHash LennaHash = dhash.hash(Lenna);
		IImage<?> rep = ImageUtils.imageRepresentation(LennaHash);
		rep = rep.rescaleNearest(64, 64);
		// BenchmarkRunner.showImage(rep);

		File destFolder = new File(folderPath);
		destFolder.mkdirs();
		if (!destFolder.exists()) {
			throw new IllegalStateException("Folder could not be craeted.");
		}

		try {
			//@nof
			ImageIO.write(rgb.toBufferedImage(), "png", 
					new File(folderPath + File.separator + "Lenna.png"));
			ImageIO.write(nineByEight.toBufferedImage(), "png",
					new File(folderPath + File.separator + "nineByEight.png"));
			ImageIO.write(greyNineByEight.toBufferedImage(), "png",
					new File(folderPath + File.separator + "greyNineByEight.png"));
			ImageIO.write(rep.toBufferedImage(), "png", 
					new File(folderPath + File.separator + "LennaHash.png"));
			//@dof
			System.out.println(LennaHash.toString().split(",")[2]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
