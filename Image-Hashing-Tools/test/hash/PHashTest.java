package hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import hash.implementations.PerceptualHash;
import image.IImage;
import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import image.implementations.SourcedImage;
import image.implementations.YCbCrImage;
import pipeline.sources.operator.IImageOperation;
import pipeline.sources.operator.ImageOperator;
import utils.TestUtils;

public class PHashTest {

	@Test
	void trivialDCTTest() {
		try {
			double[] c = new double[2];
			c[0] = 1 / Math.sqrt(2.0);
			for (int i = 1; i < 2; i++) {
				c[i] = 1;
			}

			// @nof
			double[][] f = new double[][] { 
				new double[] {54.0, 35.0}, 
				new double[] {128.0, 185.0}};
			// @dof

			double[][] encoded = DCTUtils.DCTII(f, 2, c);

			double[][] decoded = DCTUtils.IDCTII(encoded, 2, c);

			assertTrue(Arrays.deepEquals(f, decoded));

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	void matchesSimilar() {
		try {
			RGBAImage img1 = new RGBAImage(
					new URL("https://safebooru.org//images/2866/6a56e9918489442d7ba413f438a2fb753f494fe1.jpg"));
			GreyscaleImage img2 = new GreyscaleImage(
					new URL("https://safebooru.org//images/2866/10ecddb0bb8b11ed3bc2196f76c0fabeee00f77e.jpg"));

			PerceptualHash pHash = new PerceptualHash();

			assertTrue(pHash.matches(img1, img2));

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	void toFromStringTest() {

		try {
			RGBAImage img = TestUtils.safeScraper.nextImage().toRGBA();

			int sideLength = 53;

			ImageHash h1, h2;
			h1 = new PerceptualHash(sideLength).hash(img);
			h2 = ImageHash.fromString(h1.toString());

			assertEquals(h1, h2);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	@Test
	void trimHalfEquivalenceTest() {
		try {
			int imgnum = 10;
			int size = 32;
			double[] fullDCTCoefficients = DCTUtils.createDCTCoefficients(size);
			double[] halfDCTCoefficients = DCTUtils.createHalfDCTCoefficients(size);

			
			ImageOperator op = new ImageOperator(TestUtils.safeScraper, new IImageOperation() {
				@Override
				public IImage<?> operate(IImage<?> img) {
					if (img instanceof SourcedImage) {
						return this.handleSourced((SourcedImage) img, this);
					}

					double[][] original = packPixels(YCbCrImage.computeY(img), size);

					double[][] fulltrimmed = trimDCT(DCTUtils.DCTII(original, size, fullDCTCoefficients));
					double[][] half = DCTUtils.halfDCTII(original, size, halfDCTCoefficients);
					assertTrue(Arrays.deepEquals(fulltrimmed, half));

					return img;
				}
			});

			for (int i = 0; i < imgnum; i++) {
				op.nextImage();
			}
			op.close();

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	// Methods Copy/Pasted from PerceptualHash
	private static double[][] packPixels(IImage<?> img, int size) {
		byte[] bpixels = img.resizeBilinear(size, size).toGreyscale().getPixels();
		double[][] dpixels = new double[size][size];

		int offset = 0;
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				dpixels[y][x] = (double) bpixels[offset++];
			}
		}

		return dpixels;
	}

	private static double[][] trimDCT(double[][] transformed) {
		int size = transformed.length;

		// trimmedSize is half rounded up if the original was odd.
		int trimmedSize = 0;
		trimmedSize += size / 2;
		if ((size & 0x1) == 1) {
			trimmedSize += 1;
		}

		double[][] trimmedDCT = new double[trimmedSize][trimmedSize];
		for (int y = 0; y < trimmedDCT.length; y++) {
			trimmedDCT[y] = Arrays.copyOfRange(transformed[y], 0, trimmedDCT.length);
		}

		return trimmedDCT;
	}
}
