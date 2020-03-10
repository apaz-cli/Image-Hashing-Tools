package pipeline.sources.impl.joined;

import java.util.Arrays;
import java.util.List;

import image.IImage;
import pipeline.sources.ImageSource;

public class JoinedImageSource implements ImageSource {

	private int sourceCounter = 0;
	private List<ImageSource> sources;

	public JoinedImageSource(ImageSource... imageSources) throws IllegalArgumentException {
		this.sources = new CircularVector<>(Arrays.asList(imageSources));
	}

	/**
	 * Fear creating cycles with JoinedImageSources. If you add this object into
	 * itself, or into a JoinedImageSource that this object contains, you may enter
	 * an infinite loop when you call nextImage().
	 */
	public void addSource(ImageSource source) {
		synchronized (sources) {
			if (validSource(source)) {
				sources.add(source);
			}
		}
	}
	
	// Returns true if valid to add
	boolean validSource(ImageSource source) {
		// Already synchronized on sources
		if (this == source) {
			return false;
		}
		for(ImageSource is : sources) {
			if (is instanceof JoinedImageSource) {
				JoinedImageSource src = (JoinedImageSource) is;
				if (!src.validSource(source)) {
					return false;
				}
			}
		}
		return true;
	}

	public IImage<?> nextImage() {
		synchronized (sources) {
			IImage<?> img = null;
			while (img == null) {
				if (sources.isEmpty()) {
					return null;
				}
				ImageSource source = sources.get(sourceCounter++);
				synchronized (source) {
					img = source.nextImage();
					if (img == null) {
						sources.remove(source);
					}
				}
			}
			return img;
		}
	}

	public void close() {
		synchronized (sources) {
			sources = new CircularVector<>();
		}
	}

}
