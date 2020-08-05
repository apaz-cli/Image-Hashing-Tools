package pipeline.sources.single;

import java.util.Spliterator;

import image.PixelUtils;
import image.implementations.SourcedImage;
import pipeline.ImageSource;

public class SingleImageSource implements ImageSource {

	private SourcedImage img;

	public SingleImageSource(SourcedImage img) {
		PixelUtils.assertNotNull(img);
		this.img = img;
	}

	@Override
	public int characteristics() {
		return SIZED | DISTINCT | NONNULL;
	}

	@Override
	public long estimateSize() {
		return img == null ? 0 : 1;
	}

	@Override
	public SourcedImage next() {
		SourcedImage img = this.img;
		this.img = null;
		return img;
	}

	@Override
	public Spliterator<SourcedImage> trySplit() {
		return null;
	}

}
