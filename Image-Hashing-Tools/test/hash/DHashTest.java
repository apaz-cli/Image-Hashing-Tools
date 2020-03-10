package hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.Test;

import hash.implementations.DifferenceHash;
import image.implementations.GreyscaleImage;
import utils.TestUtils;

public class DHashTest {

	@Test
	void DifferenceHashToFromStringTest() {
		try {
			GreyscaleImage img = new GreyscaleImage(TestUtils.safeScraper.nextImage().toGreyscale());

			Random r = new Random();

			ImageHash h1, h2;
			
			for (int i = 0; i < 500; i++) {
				int sideLength = 0;
				while (sideLength == 0) {
					sideLength = r.nextInt(1000);
				}

				h1 = new DifferenceHash(sideLength).hash(img);
				h2 = ImageHash.fromString(h1.toString());
				assertEquals(h1, h2);
			}
			
			// Now try with a relatively large prime.
			h1 = new DifferenceHash(23197).hash(img);
			h2 = ImageHash.fromString(h1.toString());
			assertEquals(h1, h2);
			
			assertThrows(java.lang.IllegalArgumentException.class, () -> {
				new DifferenceHash(90213).hash(img);
			});
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
