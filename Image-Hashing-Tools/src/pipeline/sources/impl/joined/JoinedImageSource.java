package pipeline.sources.impl.joined;

import java.util.Arrays;
import java.util.List;

import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;

public class JoinedImageSource implements ImageSource {

	private int sourceCounter = 0;
	private List<ImageSource> sources;

	public JoinedImageSource(ImageSource... imageSources) throws IllegalArgumentException {
		this.sources = new CircularVector<>(Arrays.asList(imageSources));
	}

	public void addSource(ImageSource source) {
		sources.add(source);
	}

	public SourcedImage nextImage() {
		synchronized (sources) {
			SourcedImage img = null;
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
		sources = new CircularVector<>();
	}

}
