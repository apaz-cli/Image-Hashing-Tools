package pipeline.sources.impl.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;

import image.implementations.SourcedImage;
import pipeline.sources.ImageSource;
import pipeline.sources.impl.SourceUtil;

public class ImageCollection implements ImageSource {

	Collection<SourcedImage> coll;

	/**
	 * Accepts collections of IImage, SourcedImage, BufferedImage.
	 * 
	 * This object is not backed by a defensive copy. The actual collection passed
	 * to the constructor backs it, because a list of images will typically be too
	 * large to copy. Ideally this would be backed by a copy, but it is not for
	 * cases when memory is limited.
	 * 
	 * Because of that, The collection is not meant to be modified directly outside
	 * of this object. Please don't. If you want to fill an ImageSource collection
	 * with images from elsewhere, please use ImageBuffer or one of the other
	 * available options and join it onto this one with JoinedImageSource.
	 */
	public ImageCollection(Collection<?> imgColl) {
		Objects.requireNonNull(imgColl);
		Collection<SourcedImage> casted = imgColl.parallelStream().map(SourceUtil::castToSourced)
				.collect(Collectors.toList());
		synchronized (this) {
			coll = casted;
		}
	}

	@Override
	public SourcedImage nextImage() {
		SourcedImage item = null;
		synchronized (this) {
			if (coll == null) {
				return null;
			}
			Iterator<SourcedImage> it = coll.iterator();
			if (it.hasNext()) {
				item = it.next();
				it.remove();
			} else {
				return null;
			}
		}
		return item;
	}

	@Override
	public void close() {
		// Ensures that there is no reference to the backing collection anywhere outside
		// the object.
		synchronized (this) {
			this.coll = null;
		}
	}
}
