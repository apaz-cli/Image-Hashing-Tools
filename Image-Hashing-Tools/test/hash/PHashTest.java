package hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.jupiter.api.Test;

import hash.implementations.PerceptualHash;
import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import utils.TestUtils;

public class PHashTest {

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
			RGBAImage img = TestUtils.safeScraper.next().toRGBA();

			int sideLength = 52;

			ImageHash h1, h2;
			h1 = new PerceptualHash(sideLength).hash(img);
			h2 = ImageHash.fromString(h1.toString());

			assertEquals(h1, h2);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

}
