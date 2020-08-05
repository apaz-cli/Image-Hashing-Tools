package hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import hash.implementations.DifferenceHash;
import utils.TestUtils;

public class ImageHashTest {

	@Test
	void toFromStringTest() throws IllegalArgumentException, ClassNotFoundException {
		ImageHash h = new DifferenceHash().hash(TestUtils.safeScraper.next());
		assertEquals(h, ImageHash.fromString(h.toString()));
	}

}
