package image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import utils.TestUtils;

public class IImageTest {

	private static List<IImage<?>> testImages = null;

	static {
		testImages = new ArrayList<IImage<?>>();
		testImages.add(TestUtils.safeScraper.next().toRGBA());
		testImages.add(TestUtils.safeScraper.next().toRGBA());
	}

	@Test
	void emplaceReplaceIImageTest() {
		RGBAImage img1 = testImages.get(0).toRGBA();
		GreyscaleImage img2 = testImages.get(0).toGreyscale();

		RGBAImage rgbaimg2 = img1.resizeNearest(img2.getWidth(), img2.getHeight()).emplaceSubimage(img2.toRGBA(),
				new Point(0, 0), new Point(img2.getWidth() - 1, img2.getHeight() - 1));
		assertEquals(rgbaimg2, img2.toRGBA());

		GreyscaleImage greyimg1 = img2.resizeNearest(img1.getWidth(), img1.getHeight()).emplaceSubimage(
				img1.toGreyscale(), new Point(0, 0), new Point(img1.getWidth() - 1, img1.getHeight() - 1));
		assertEquals(greyimg1, img2);
	}
}
