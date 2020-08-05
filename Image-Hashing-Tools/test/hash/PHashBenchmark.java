package hash;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import attack.convolutions.EdgeMode;
import attack.convolutions.KernelFactory;
import hash.implementations.DifferenceHash;
import image.implementations.RGBImage;

public class PHashBenchmark {

	@Test
	public void bench() throws MalformedURLException, IOException {
		RGBImage img = new RGBImage(new URL("http://tutorial.simplecv.org/en/latest/_images/lenna-morphclose.png"));
		RGBImage blurred = img.apply(new KernelFactory<RGBImage>().averageBlurKernel(3, EdgeMode.DOWNSIZE));

		IHashAlgorithm phash = new DifferenceHash(8);

		// long t0 = System.currentTimeMillis();

		System.out.println(phash.hash(img));
		System.out.println(phash.hash(blurred));

		// long t1 = System.currentTimeMillis();
		// long diff = t1 - t0;
	}

}
