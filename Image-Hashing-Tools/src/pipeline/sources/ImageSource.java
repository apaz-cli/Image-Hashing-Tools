package pipeline.sources;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import image.IImage;
import pipeline.sources.impl.SourceUtil;

public interface ImageSource extends Closeable {
	public abstract SourcedImage nextImage();

	public default IImage<?> nextIImage() {
		SourcedImage img = this.nextImage();
		return img == null ? null : img.unwrap();
	}

	public default BufferedImage nextBufferedImage() {
		SourcedImage img = this.nextImage();
		return img == null ? null : img.unwrapBufferedImage();
	}

	public default List<SourcedImage> toSourcedList() {
		ArrayList<SourcedImage> list = new ArrayList<>();
		SourcedImage img;
		while ((img = this.nextImage()) != null) {
			list.add(img);
		}
		return list;
	}

	public default List<IImage<?>> toIImageList() {
		ArrayList<IImage<?>> list = new ArrayList<>();
		IImage<?> img;
		while ((img = this.nextIImage()) != null) {
			list.add(img);
		}
		return list;
	}

	public default List<BufferedImage> toBufferedImageList() {
		ArrayList<BufferedImage> list = new ArrayList<>();
		BufferedImage img;
		while ((img = this.nextBufferedImage()) != null) {
			list.add(img);
		}
		return list;
	}

	/**
	 * Once close() is called, release all references to objects, free all resources
	 * such as thread pools, open files and socket connections, and nextImage()
	 * methods should always return null.
	 * 
	 * However, if the ImageSource implemented has other ImageSources inside, it
	 * need not close them too. It must only give up references.
	 */
	@Override
	public abstract void close();
}
