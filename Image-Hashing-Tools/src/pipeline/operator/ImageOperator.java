package pipeline.operator;

import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import image.implementations.SourcedImage;
import pipeline.ImageSource;

public class ImageOperator implements ImageSource {

	private ImageSource source;
	private ImageOperation<SourcedImage>[] imageOperations;

	@SafeVarargs
	public ImageOperator(ImageSource source, ImageOperation<SourcedImage>... operations) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(operations);
		for (ImageOperation<SourcedImage> op : operations) {
			Objects.requireNonNull(op);
		}

		this.source = source;
		this.imageOperations = operations;
	}

	@Override
	public SourcedImage next() {
		SourcedImage img = source.next();
		return (img == null) ? null : applyOperations(img);
	}

	@Override
	public boolean tryAdvance(Consumer<? super SourcedImage> action) {
		SourcedImage img = source.next();
		if (img == null) return false;
		action.accept(applyOperations(img));
		return true;
	}

	public SourcedImage applyOperations(SourcedImage img) {

		ImageOperation<SourcedImage>[] ops = null;
		synchronized (this) {
			// Synchronized so that it can't be closed during copy.

			// If closed already, do nothing.
			if (this.imageOperations == null) { return null; }

			// It seems strange to do a copy every time. You'd think that it would add up,
			// but this isn't actually very expensive at all.
			ops = Arrays.copyOf(this.imageOperations, this.imageOperations.length);
		}

		// Apply all the operations. Since we copied, this is now thread-safe.
		SourcedImage operated = img;
		for (ImageOperation<SourcedImage> op : ops) {
			operated = operated.apply(op);
		}

		return operated;
	}

	@Override
	public int characteristics() {
		return source.characteristics();
	}

	@Override
	public long estimateSize() {
		return source.estimateSize();
	}

	@Override
	public Spliterator<SourcedImage> trySplit() {
		Spliterator<SourcedImage> spt = source.trySplit();
		if (spt == null) return null;
		return new ImageOperator((ImageSource) spt, imageOperations);
	}

}
