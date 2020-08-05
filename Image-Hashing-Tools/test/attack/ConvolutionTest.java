package attack;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Random;

import org.junit.jupiter.api.Test;

import attack.convolutions.EdgeMode;
import attack.convolutions.InseperableKernel;
import attack.convolutions.SeperableKernel;
import image.implementations.GreyscaleImage;
import image.implementations.RGBImage;
import utils.TestUtils;

public class ConvolutionTest {

	@Test
	void seperabilityEquivalenceTest() {
		Random r = new Random();

		for (int tries = 0; tries < 25; tries++) {
			int sideLength = 0;
			while ((sideLength & 0x1) == 0) {
				sideLength = r.nextInt(12);
			}
			
			EdgeMode mode = r.nextBoolean() ? EdgeMode.DOWNSIZE : EdgeMode.WRAP;

			float[] kerX = new float[sideLength];
			float[] kerY = new float[sideLength];

			for (int i = 0; i < sideLength; i++) {
				kerX[i] = r.nextFloat();
				kerY[i] = r.nextFloat();
			}

			SeperableKernel<RGBImage> ker1 = new SeperableKernel<>(kerX, kerY, mode);
			InseperableKernel<RGBImage> ker2 = ker1.toInseperable();

			RGBImage img = TestUtils.safeScraper.next().toRGB();

			GreyscaleImage img1 = img.apply(ker1).toGreyscale();
			GreyscaleImage img2 = img.apply(ker2).toGreyscale();

			// Check that they're within 1 of each other, to account for floating point
			// error and such.
			int tolerance = 1, diff;
			byte[] p1 = img1.getPixels(), p2 = img2.getPixels();
			for (int i = 0; i < p1.length; i++) {
				diff = (p1[i] & 0xff) - (p2[i] & 0xff);
				if (diff > tolerance || diff < -tolerance) {
					fail();
				}
			}
			assertTrue(true);
		}
	}
}
