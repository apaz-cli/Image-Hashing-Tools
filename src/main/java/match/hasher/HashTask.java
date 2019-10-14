package match.hasher;

import java.awt.image.BufferedImage;

import hash.IHashAlgorithm;
import image.IImage;
import image.implementations.RGBImage;
import utils.BenchmarkRunner;

public class HashTask implements Runnable {

	private IHashAlgorithm algorithm;
	private BufferedImage bi;
	private IImage<?> image;
	private ImageHasher taskOwner;

	// Don't check for null. Do it later concurrently.
	public HashTask(ImageHasher taskOwner, BufferedImage img, IHashAlgorithm algorithm) {
		this.taskOwner = taskOwner;
		this.bi = img; // Don't convert yet, do it concurrently in run()
		this.algorithm = algorithm;
	}

	public HashTask(ImageHasher taskOwner, IImage<?> img, IHashAlgorithm algorithm) {
		this.taskOwner = taskOwner;
		this.image = img; // No conversion necessary
		this.algorithm = algorithm;
	}

	@Override
	public void run() {
		// Ensure image is set
		
		// TODO get rid of try
		try {
		
		if (this.image == null) {
			if (this.bi != null) {
				this.image = new RGBImage(this.bi);
			} else {
				new IllegalStateException("The image passed to HashTask's constructor was null.").printStackTrace();
				return;
			}
		}

		// Save hash or add to list
		taskOwner.recieveHash(this.algorithm.hash(this.image));
		} catch (Exception e) {
			BenchmarkRunner.showImage(this.bi, e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

}
