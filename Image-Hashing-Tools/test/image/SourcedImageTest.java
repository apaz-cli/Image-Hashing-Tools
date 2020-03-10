package image;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.net.URL;

import org.junit.jupiter.api.Test;

import hash.IHashAlgorithm;
import hash.MatchMode;
import hash.implementations.PerceptualHash;
import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import image.implementations.RGBImage;
import image.implementations.SourcedImage;

public class SourcedImageTest {

	private static int imageNum = 4;

	private static SourcedImage[] testImages = new SourcedImage[imageNum];
	private static URL[] testURLs = new URL[imageNum];
	static {
		try {
			testURLs[0] = new URL(
					"https://safebooru.org//images/2869/e1179396aaad897a0177a274b8367b97dcee5afa.jpg?2988638");
			testURLs[1] = new URL(
					"https://safebooru.org//images/2868/3f20166259d2a2e644c23c2057976507d7a16374.png?2987536");
			testURLs[2] = new URL(
					"https://safebooru.org//images/2867/f2d4e0434e524d58a5f227c61a772081ac36a253.jpg?2986430");
			testURLs[3] = new URL(
					"https://safebooru.org//images/2788/5d799cfa502f36d318c62d30f22a2707a1c68300.png?2904213");

			testImages[0] = new SourcedImage(new GreyscaleImage(testURLs[0]), testURLs[0]).resizeBilinear(250, 250);
			testImages[1] = new SourcedImage(new RGBImage(testURLs[1]), testURLs[1]).resizeBilinear(250, 250);
			testImages[2] = new SourcedImage(new RGBAImage(testURLs[2]), testURLs[2]).resizeBilinear(250, 250);
			testImages[3] = new SourcedImage(new SourcedImage(new RGBAImage(testURLs[3]), testURLs[3]), testURLs[3])
					.resizeBilinear(250, 250);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void sourcedEmplaceCastTest() {

		IHashAlgorithm alg = new PerceptualHash();

		for (int i = 0; i < imageNum; i++) {
			for (int j = 0; j < imageNum; j++) {
				SourcedImage emplaced = 
				testImages[i].emplaceSubimage(testImages[j], new Point(0, 0), new Point(249, 249));

				assertTrue(alg.matches(testImages[j], emplaced, MatchMode.STRICT));
				assertEquals(emplaced.getSource(), testImages[i].getSource());

				if (emplaced.unwrap() instanceof SourcedImage) {
					assertEquals(((SourcedImage) emplaced.unwrap()).getSource(),
								 ((SourcedImage) testImages[i].unwrap()).getSource());
				}

			}
		}

	}

}
