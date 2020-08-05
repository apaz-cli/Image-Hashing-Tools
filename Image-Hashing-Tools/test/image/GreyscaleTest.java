package image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import image.implementations.GreyscaleImage;
import utils.TestUtils;

public class GreyscaleTest {

	private static GreyscaleImage testImage = TestUtils.safeScraper.next().toGreyscale();

	@Test
	void rotationFlipEquivalenceTest() {
		assertEquals(testImage, testImage.flipHorizontal().flipHorizontal());
		assertEquals(testImage, testImage.flipVertical().flipVertical());
		assertEquals(testImage, testImage.rotate90CCW().rotate90CW());
		assertEquals(testImage, testImage.rotate180().rotate180());
		assertEquals(testImage, testImage.flipHorizontal().rotate90CW().flipVertical().rotate90CCW());
		assertEquals(testImage.rotate180(), testImage.rotate90CCW().rotate90CCW());
	}

	@Test
	void extractSubImageTest() {
		int a = 3, b = 7, c = 337, d = 250;
		Point p1 = new Point(a, b), p2 = new Point(c, d);
		Point p3 = new Point(a, d), p4 = new Point(c, b);
		assertEquals(testImage.extractSubimage(p2, p1), testImage.extractSubimage(p1, p2));
		assertEquals(testImage.extractSubimage(p1, p2), testImage.extractSubimage(p3, p4));
		assertEquals(testImage, testImage.extractSubimage(new Point(0, 0),
				new Point(testImage.getWidth() - 1, testImage.getHeight() - 1)));
	}

	@Test
	void emplaceSubImageTest() {
		int a = 10, b = 15, c = 17, d = 20;
		Point p1 = new Point(a, b), p2 = new Point(c, d);
		Point p3 = new Point(a, d), p4 = new Point(c, b);

		int[] barr = IntStream.rangeClosed(1, 48).toArray();
		byte[] pixels = new byte[barr.length];
		int offset = 0;
		for (int by : barr) {
			pixels[offset++] = (byte) by;
		}
		GreyscaleImage g = new GreyscaleImage(pixels, 8, 6);

		assertEquals(testImage.emplaceSubimage(g, p1, p2), testImage.emplaceSubimage(g, p3, p4));

		assertEquals(g, testImage.emplaceSubimage(g, p1, p2).extractSubimage(p1, p2));
	}
}
