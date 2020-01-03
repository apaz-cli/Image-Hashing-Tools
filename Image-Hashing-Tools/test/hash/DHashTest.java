package hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Random;

import org.junit.jupiter.api.Test;

import hash.implementations.DifferenceHash;
import image.implementations.GreyscaleImage;

public class DHashTest {

	@Test
	void DifferenceHashToFromStringTest() {
		try {
			GreyscaleImage img = new GreyscaleImage(new URL("https://homepages.cae.wisc.edu/~ece533/images/cat.png"));

			Random r = new Random();
			
			for (int i = 0; i < 500; i++) {
				int sideLength = 0;
				while (sideLength == 0) {
					sideLength = r.nextInt(10000);
				}

				ImageHash h = new DifferenceHash(sideLength).hash(img);
				assertEquals(h, ImageHash.fromString(h.toString()));
			}

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}
}
