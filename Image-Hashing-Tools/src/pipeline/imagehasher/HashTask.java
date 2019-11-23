package pipeline.imagehasher;

import java.awt.image.BufferedImage;

import hash.IHashAlgorithm;
import image.IImage;
import image.implementations.RGBImage;
import pipeline.sources.SourcedImage;

public class HashTask implements Runnable {

	private IHashAlgorithm algorithm;
	private BufferedImage bi;
	private IImage<?> image;
	private ImageHasher taskOwner;
	private String source;

	// In general, don't check for null. Just assign, and do it later concurrently.
	public HashTask(ImageHasher taskOwner, BufferedImage img, IHashAlgorithm algorithm) {
		this.taskOwner = taskOwner;
		this.bi = img; // Don't convert yet, do it concurrently in run()
		this.algorithm = algorithm;
	}

	public HashTask(ImageHasher taskOwner, BufferedImage img, String source, IHashAlgorithm algorithm) {
		this.taskOwner = taskOwner;
		this.bi = img;
		this.source = source;
		this.algorithm = algorithm;
	}

	public HashTask(ImageHasher taskOwner, SourcedImage img, IHashAlgorithm algorithm) {
		this.taskOwner = taskOwner;
		this.image = img.unwrap();
		this.algorithm = algorithm;
		this.source = img.getSource();
	}

	public HashTask(ImageHasher taskOwner, IImage<?> img, IHashAlgorithm algorithm) {
		this.taskOwner = taskOwner;
		this.image = img; 
		this.algorithm = algorithm;
	}

	public HashTask(ImageHasher taskOwner, IImage<?> img, String source, IHashAlgorithm algorithm) {
		this.taskOwner = taskOwner;
		this.image = img; 
		this.algorithm = algorithm;
	}

	@Override
	public void run() {
		// Ensure image is set
		if (this.image == null) {
			if (this.bi != null) {
				this.image = new RGBImage(this.bi);
			} else {
				new IllegalStateException("The image passed to HashTask's constructor was null.").printStackTrace();
				return;
			}
		}

		// Save hash or add to list
		try {
			taskOwner.recieveHash(this.algorithm.hash(this.image), this.source);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
