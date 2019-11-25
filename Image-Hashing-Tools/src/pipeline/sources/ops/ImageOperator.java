package pipeline.sources.ops;

import java.util.Objects;

import image.IImage;
import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;

public class ImageOperator implements ImageSource {

	ImageSource source;
	boolean iiFirst = true;
	IImageOperation[] iimageOperations;
	SourcedImageOperation[] sourcedImageOperations;

	public ImageOperator(ImageSource source, IImageOperation operation) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(operation);
		this.source = source;
		this.iimageOperations = new IImageOperation[] { operation };
	}

	public ImageOperator(ImageSource source, SourcedImageOperation operation) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(operation);
		this.source = source;
		this.sourcedImageOperations = new SourcedImageOperation[] { operation };
	}

	public ImageOperator(ImageSource source, IImageOperation... operations) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(operations);
		for (IImageOperation op : operations) {
			Objects.requireNonNull(op);
		}

		this.source = source;
		this.iimageOperations = operations;
	}

	public ImageOperator(ImageSource source, SourcedImageOperation... operations) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(operations);
		for (SourcedImageOperation op : operations) {
			Objects.requireNonNull(op);
		}
		this.source = source;
		this.iimageOperations = null;
		this.sourcedImageOperations = operations;
	}

	public ImageOperator(ImageSource source, IImageOperation[] iiOperations, SourcedImageOperation... siOperations) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(iiOperations);
		Objects.requireNonNull(siOperations);
		for (IImageOperation op : iiOperations) {
			Objects.requireNonNull(op);
		}
		for (SourcedImageOperation op : siOperations) {
			Objects.requireNonNull(op);
		}

		this.source = source;
		this.iimageOperations = iiOperations;
		this.sourcedImageOperations = siOperations;
	}

	public ImageOperator(ImageSource source, SourcedImageOperation[] siOperations, IImageOperation... iiOperations) {
		this(source, iiOperations, siOperations);
		iiFirst = false;
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

		if (img == null) {
			return null;
		}

		// Apply operations in specified order
		if (iiFirst) {
			IImage<?> iimg = applyIIOperations(img.unwrap());
			img = applySourcedOperations(new SourcedImage(iimg, img.getSource(), img.isURL()));
		} else {
			img = applySourcedOperations(img);
			IImage<?> iimg = applyIIOperations(img.unwrap());
			img = new SourcedImage(iimg, img.getSource(), img.isURL());
		}
		return img;
	}

	private SourcedImage applySourcedOperations(SourcedImage img) {
		if (sourcedImageOperations == null) {
			return img;
		}

		if (img == null) {
			return null;
		}

		for (int i = 0; i < sourcedImageOperations.length; i++) {
			img = sourcedImageOperations[i].operate(img);
		}
		return img;

	}

	private IImage<?> applyIIOperations(IImage<?> img) {
		if (iimageOperations == null) {
			return img;
		}

		if (img == null) {
			return null;
		}

		for (int i = 0; i < iimageOperations.length; i++) {
			img = iimageOperations[i].operate(img);
		}
		return img;
	}

	public void executeAll() {
		@SuppressWarnings("unused")
		SourcedImage img;
		while ((img = this.nextImage()) != null) {
		}
	}

	@Override
	public synchronized void close() {
		this.source = null;
		this.iimageOperations = null;
		this.sourcedImageOperations = null;
	}

}
