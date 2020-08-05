package hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import hash.implementations.DifferenceHash;
import image.implementations.GreyscaleImage;
import utils.TestUtils;

public class DHashTest {

	@Test
	void DifferenceHashToFromStringTest() {
		try {
			GreyscaleImage img = TestUtils.safeScraper.next().toGreyscale();

			ImageHash h1, h2;

			for (int i = 2; i < 500; i++) {

				h1 = new DifferenceHash(i).hash(img);
				h2 = ImageHash.fromString(h1.toString());
				assertEquals(h1, h2);
			}

			// Now try with a relatively large prime.
			h1 = new DifferenceHash(23197).hash(img);
			h2 = ImageHash.fromString(h1.toString());
			assertEquals(h1, h2);

			assertThrows(java.lang.IllegalArgumentException.class, () -> { new DifferenceHash(90213).hash(img); });

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
