package pipeline.sources;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

import hash.ImageHash;
import image.implementations.SourcedImage;
import pipeline.ImageSource;

public interface SavingImageSource extends ImageSource, Closeable {

	abstract public void removeFromSource(String source);

	default public void removeFromSource(ImageHash h) {
		this.removeFromSource(h.getSource());
	}

	default public void removeFromSource(SourcedImage img) {
		this.removeFromSource(img.getSource());
	}

	default public void removeAllFromSource(Collection<SourcedImage> imgCollection) {
		for (SourcedImage img : imgCollection) {
			this.removeFromSource(img);
		}
	}

	abstract public void addToSource(SourcedImage img);

	default public void addAllToSource(Collection<SourcedImage> imgCollection) {
		for (SourcedImage img : imgCollection) {
			this.addToSource(img);
		}
	}

	// Add and delete methods must create and handle any backlog of changes they
	// need to make such that they don't hold the images that need to be
	// added/deleted in memory.

	// An implementation of this interface can be backed by whatever it wants to be,
	// but an easy way to do this is to call save() intermittently once you're
	// holding onto say 5 images to add or 2000 urls to delete. Just make sure that
	// you don't run out of memory.

	abstract public void save() throws IOException;

	@Override
	default public void close() throws IOException {
		this.save();
	}

}
