package image;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import image.implementations.GreyscaleImage;

class GreyscaleTests {

	private static GreyscaleImage testImage = null;
	private static URL testImageURL = null;
	static {
		try {
			testImageURL = new URL("https://safebooru.org//images/1266/9b3bc732bfa59cc8427951c280d584a868bdd1d5.png");
			testImage = new GreyscaleImage(testImageURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void rotationFlipEquivalenceTests() {
		assertEquals(testImage, testImage.flipHorizontal().flipHorizontal());
		assertEquals(testImage, testImage.flipVertical().flipVertical());
		assertEquals(testImage, testImage.rotate90CCW().rotate90CW());
		assertEquals(testImage, testImage.rotate180().rotate180());
		assertEquals(testImage, testImage.flipHorizontal().rotate90CW().flipVertical().rotate90CCW());
		assertEquals(testImage.rotate180(), testImage.rotate90CCW().rotate90CCW());
	}

	@Test
	void extractSubImageTests() {
		int a = 3, b = 7, c = 337, d = 250;
		Point p1 = new Point(a, b), p2 = new Point(c, d);
		Point p3 = new Point(a, d), p4 = new Point(c, b);
		assertEquals(testImage.extractSubImage(p2, p1), testImage.extractSubImage(p1, p2));
		assertEquals(testImage.extractSubImage(p1, p2), testImage.extractSubImage(p3, p4));
		assertEquals(testImage, testImage.extractSubImage(new Point(0, 0),
				new Point(testImage.getWidth() - 1, testImage.getHeight() - 1)));
	}
}
