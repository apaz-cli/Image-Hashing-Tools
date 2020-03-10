package pipeline.sources;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import image.IImage;
import image.implementations.SourcedImage;

public interface ImageSource extends Closeable {
	public abstract IImage<?> nextImage();

	public default BufferedImage nextBufferedImage() {
		IImage<?> img = this.nextImage();
		return img == null ? null : img.toBufferedImage();
	}

	public default List<SourcedImage> toSourcedList() {
		ArrayList<SourcedImage> list = new ArrayList<>();
		IImage<?> img;
		while ((img = this.nextImage()) != null) {
			if (img instanceof SourcedImage) {
				list.add((SourcedImage) img);
			} else {
				list.add(new SourcedImage(img));
			}
		}
		return list;
	}

	public default List<IImage<?>> toIImageList() {
		ArrayList<IImage<?>> list = new ArrayList<>();
		IImage<?> img;
		while ((img = this.nextImage()) != null) {
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
