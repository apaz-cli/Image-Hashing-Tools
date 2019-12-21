package pipeline.sources.ops;

import java.util.Objects;

import image.IImage;
import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;
import pipeline.sources.TerminalImage;

public class ImageOperator implements ImageSource {

	ImageSource source;
	private ImageOperation[] imageOperations;

	public ImageOperator(ImageSource source, ImageOperation operation) {
		this(source, new ImageOperation[] { operation });
	}

	public ImageOperator(ImageSource source, ImageOperation... operations) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(operations);
		for (ImageOperation op : operations) {
			Objects.requireNonNull(op);
		}

		this.source = source;
		this.imageOperations = operations;
	}

	@Override
	public SourcedImage nextImage() {

		// Get image from backing source
		SourcedImage img = null;
		synchronized (this) {
			if (this.source == null) {
				return null;
			}
			img = source.nextImage();
		}

		if (img == null || img instanceof TerminalImage) {
			return null;
		}

		return applyOperations(img);
	}

	public SourcedImage applyOperations(SourcedImage img) {
		synchronized (this) {

			if (this.imageOperations == null) {
				return null;
			}

			synchronized (this.imageOperations) {
				for (ImageOperation op : this.imageOperations) {
					if (op instanceof IImageOperation) {
						IImage<?> operated = ((IImageOperation) op).operate(img.unwrap());
						img = new SourcedImage(operated, img.getSource(), img.isURL());
					} else if (op instanceof SourcedImageOperation) {
						img = ((SourcedImageOperation) op).operate(img);
					}
				}
			}
		}
		return img;
	}

	/**
	 * Invokes the operations provided on all images in this source does not return
	 * a list. This is a terminal operation. It simply executes the operations on
	 * all the images in the ImageSource, or until close() is called.
	 */
	public void executeAll() {
		// Does not need to be synchronized on this to deal with closing, because calls
		// to getting images and applying operations are already synchronized.
		@SuppressWarnings("unused")
		SourcedImage img;
		while ((img = this.nextImage()) != null) {
		}
	}

	@Override
	public synchronized void close() {
		synchronized (this) {
			this.source = null;
			synchronized (imageOperations) {
				this.imageOperations = null;
			}
		}
	}

}
