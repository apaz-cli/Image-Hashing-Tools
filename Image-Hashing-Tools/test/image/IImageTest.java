package image;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import image.implementations.*;

public class IImageTest {

	private static List<IImage<?>> testImages = null;

	static {
		try {
			testImages = new ArrayList<IImage<?>>();
			testImages.add(new RGBAImage(
					new URL("https://safebooru.org//images/1266/9b3bc732bfa59cc8427951c280d584a868bdd1d5.png")));
			testImages.add(new RGBAImage(
					new URL("https://img2.gelbooru.com//images/e3/ba/e3baf1801f4770c0f8738b5ab9560bb4.jpg")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void emplaceReplaceIImageTest() {

		RGBAImage img1 = testImages.get(0).toRGBA();
		GreyscaleImage img2 = testImages.get(0).toGreyscale();
		
		RGBAImage rgbaimg2 = img1.resizeNearest(img2.getWidth(), img2.getHeight()).emplaceSubimage(img2.toRGBA(), new Point(0,0), new Point(img2.getWidth()-1, img2.getHeight()-1));
		assertEquals(rgbaimg2, img2.toRGBA());
		
		GreyscaleImage greyimg1 = img2.resizeNearest(img1.getWidth(), img1.getHeight()).emplaceSubimage(img1.toGreyscale(), new Point(0,0), new Point(img1.getWidth()-1, img1.getHeight()-1));
		assertEquals(greyimg1, img2);
	}
}
