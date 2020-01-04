package utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.implementations.AverageHash;
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
			throw new IllegalStateException("Failed to load Lenna image.");
		}
	}

	public static void main(String[] args) {
		String exampleFolderPath = "C:\\Users\\PazderaAaron\\Downloads\\Image Hashing Related\\Examples";
		dhashDemo(exampleFolderPath + "\\dhash");
		ahashDemo(exampleFolderPath + "\\ahash");
	}

	private static void dhashDemo(String folderPath) {
		IHashAlgorithm dhash = new DifferenceHash();

		RGBImage rgb = new RGBImage(Lenna);

		RGBImage nineByEight = rgb.resizeBilinear(9, 8);
		nineByEight = nineByEight.rescaleNearest(64, 64);

		GreyscaleImage greyNineByEight = nineByEight.toGreyscale();
		greyNineByEight = greyNineByEight.rescaleNearest(64, 64);

		ImageHash LennaHash = dhash.hash(Lenna);
		IImage<?> rep = ImageUtils.imageRepresentation(LennaHash, 8, 8);
		rep = rep.rescaleNearest(64, 64);

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
					new File(folderPath + File.separator + "Lenna 9x8.png"));
			ImageIO.write(greyNineByEight.toBufferedImage(), "png",
					new File(folderPath + File.separator + "Grey Lenna 9x8.png"));
			ImageIO.write(rep.toBufferedImage(), "png", 
					new File(folderPath + File.separator + "LennaHash.png"));
			//@dof
			System.out.println("Lenna dhash: " + LennaHash.toString().split(",")[2]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void ahashDemo(String folderPath) {
		IHashAlgorithm dhash = new AverageHash();

		RGBImage rgb = new RGBImage(Lenna);
		// BenchmarkRunner.showImage(rgb);

		RGBImage eightByEight = rgb.resizeBilinear(8, 8);
		eightByEight = eightByEight.rescaleNearest(64, 64);
		// BenchmarkRunner.showImage(eightByEight);

		GreyscaleImage greyEightByEight = eightByEight.toGreyscale();
		greyEightByEight = greyEightByEight.rescaleNearest(64, 64);
		// BenchmarkRunner.showImage(greyNineByEight);

		ImageHash LennaHash = dhash.hash(Lenna);
		IImage<?> rep = ImageUtils.imageRepresentation(LennaHash, 8, 8);
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
			ImageIO.write(eightByEight.toBufferedImage(), "png",
					new File(folderPath + File.separator + "Lenna 8x8.png"));
			ImageIO.write(greyEightByEight.toBufferedImage(), "png",
					new File(folderPath + File.separator + "Grey Lenna 8x8.png"));
			ImageIO.write(rep.toBufferedImage(), "png", 
					new File(folderPath + File.separator + "LennaHash.png"));
			//@dof
			System.out.println("Lenna ahash: " + LennaHash.toString().split(",")[2]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
