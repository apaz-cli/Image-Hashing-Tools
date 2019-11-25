package pipeline.sources.impl.buffer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;
import pipeline.sources.impl.SourceUtil;

public class ImageBuffer implements ImageSource {

	private boolean filled = false;
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
	 * A null argument marks this ImageSource as filled. If you're in putting
	 * objects from multiple sources and don't want to close is buffer when a
	 * different buffer ends, then call emplaceOpen() instead.
	 */
	public void emplace(Object img) throws IllegalStateException {
		SourcedImage emp = SourceUtil.castToSourced(img);
		synchronized (this) {
			if (img == null) {
				this.markFilled();
				return;
			}

			if (this.buffer == null) {
				throw new IllegalStateException("This object is already closed.");
			}

			if (this.filled) {
				throw new IllegalStateException("This object is already marked as filled.");
			}

			synchronized (buffer) {
				buffer.add(emp);
				buffer.notify();
			}
		}
	}

	public void emplaceOpen(Object img) throws IllegalStateException {
		if (img == null) {
			return;
		}
		this.emplace(img);
	}

	public void emplace(Collection<?> imgCollection) throws IllegalStateException {
		synchronized (this) {
			if (imgCollection == null) {
				this.markFilled();
				return;
			} else if (this.filled) {
				throw new IllegalStateException("This object is already marked as filled and/or closed.");
			}
			synchronized (imgCollection) {
				imgCollection.forEach((img) -> {
					this.emplace(img);
				});
			}
		}
	}

	public void emplaceOpen(Collection<?> imgCollection) throws IllegalStateException {
		if (imgCollection == null) {
			return;
		}
		this.emplace(imgCollection);
	}

	public void markFilled() {
		synchronized (this) {
			this.filled = true;
		}
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
					if (!this.filled) {
						try {
							buffer.wait();
						} catch (InterruptedException e) {
						}
					} else {
						// If empty and no more coming, finally return null.
						return null;
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

			this.filled = true;

			synchronized (this.buffer) {
				this.buffer.notifyAll();
			}

			// I have so synchronize on both this and buffer so that I can make this one
			// single reassignment.
			this.buffer = null;
		}
	}

}
