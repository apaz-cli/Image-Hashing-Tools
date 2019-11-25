package pipeline.sources.impl.buffer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;
import pipeline.sources.impl.SourceUtil;

public class ImageBuffer implements ImageSource {

	private List<SourcedImage> buffer = null;

	public ImageBuffer() {
		this.buffer = new LinkedList<>();
	}

	public ImageBuffer(Collection<?> imgCollection) {
		this.buffer = buffer.parallelStream().map(SourceUtil::castToSourced).collect(Collectors.toList());
	}

	/**
	 * Accepts BufferedImage, IImage<?>, SourcedImage
	 * 
	 * A null argument closes this ImageSource. If you're in putting objects from
	 * multiple sources and don't want to close is buffer when a different buffer
	 * ends, then call emplaceOpen() instead.
	 */
	public void emplace(Object img) throws IllegalStateException {
		SourcedImage emp = SourceUtil.castToSourced(img);
		synchronized (this) {
			if (img == null) {
				this.close();
				return;
			}

			if (this.buffer == null) {
				throw new IllegalStateException("This object is already closed.");
			}
			synchronized (buffer) {
				buffer.add(emp);
				buffer.notify();
				System.out.println("PUT IN BUFFER");
			}
		}
	}

	public void emplaceOpen(Object img) throws IllegalStateException {
		if (img == null) {
			return;
		}
		this.emplace(img);
	}

	public void emplace(Collection<?> imgCollection) {
		if (imgCollection == null) {
			return;
		}
		synchronized (imgCollection) {
			imgCollection.forEach((img) -> {
				this.emplace(img);
			});
		}
	}

	public void emplaceOpen(Collection<?> imgCollection) throws IllegalStateException {
		// It isn't possible to have null in a collection, so we can just pass it on.
		// Also, if null, emplace(Collection<?>) will handle it.
		this.emplace(imgCollection);
	}

	@Override
	public SourcedImage nextImage() {
		SourcedImage img = null;
		synchronized (this) {
			if (this.buffer == null) {
				return null;
			}
			synchronized (buffer) {
				if (buffer.isEmpty()) {
					try {
						buffer.wait();
					} catch (InterruptedException e) {
					}
				}
				img = buffer.remove(0);
			}
		}
		return img;
	}

	@Override
	public void close() {
		synchronized (this) {
			if (this.buffer == null) {
				return;
			}

			synchronized (buffer) {
				this.buffer.notifyAll();
			}

			// I have so synchronize on both this and buffer so that I can make this one
			// single reassignment.
			this.buffer = null;
		}
	}
}
