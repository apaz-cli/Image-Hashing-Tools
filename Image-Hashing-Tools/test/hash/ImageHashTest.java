package hash;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import hash.ImageHash;
import hash.implementations.DifferenceHash;
import utils.TestUtils;

public class ImageHashTest {

	@Test
	void toFromStringTest() {
		ImageHash h = new DifferenceHash().hash(TestUtils.safeScraper.nextImage());
		assertEquals(h, ImageHash.fromString(h.toString()));
	}

}
