package pipeline.sources.impl.buffer;

import java.util.LinkedList;
import java.util.List;

import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;
import pipeline.sources.impl.SourceUtil;

public class ImageBuffer implements ImageSource {

	private Object closeFlag = new Object();
	private List<SourcedImage> buffer = new LinkedList<>();

	public void emplace(Object img) {
		SourcedImage emp = SourceUtil.castToSourced(img);
		synchronized (buffer) {
			buffer.add(emp);
			buffer.notify();
		}
	}

	@Override
	public SourcedImage nextImage() {
		SourcedImage img = null;
		synchronized (buffer) {
			if (buffer.isEmpty()) {
				try {
					buffer.wait();
				} catch (InterruptedException e) {
				}
			}
			// This will only not be true when close() is called on an empty buffer.
			if (!buffer.isEmpty()) {
				img = buffer.remove(0);
			} else {
				closeFlag.notify();
			}
		}
		return img;
	}

	@Override
	public void close() {
		synchronized (buffer) {
			buffer.notifyAll();
		}
		try {
			closeFlag.wait(500);
		} catch (InterruptedException e) {
		}
		synchronized (buffer) {
			buffer = new LinkedList<>();
		}
	}
}
