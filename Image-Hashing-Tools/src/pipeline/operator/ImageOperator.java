package pipeline.operator;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import image.implementations.SourcedImage;
import pipeline.ImageSource;

public class ImageOperator implements ImageSource {

	private final ImageSource source;
	private final ImageOperation<SourcedImage>[] imageOperations;

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
		SourcedImage operated = img;
		for (ImageOperation<SourcedImage> op : this.imageOperations) {
			operated = operated.apply(op);
		}

		return operated;
	}

	@Override
	public int characteristics() { return source.characteristics(); }

	@Override
	public long estimateSize() { return source.estimateSize(); }

	@Override
	public String getSourceName() { return source.getSourceName(); }

	@Override
	public Spliterator<SourcedImage> trySplit() {
		Spliterator<SourcedImage> spt = source.trySplit();
		if (spt == null) return null;
		return new ImageOperator((ImageSource) spt, imageOperations);
	}

}
