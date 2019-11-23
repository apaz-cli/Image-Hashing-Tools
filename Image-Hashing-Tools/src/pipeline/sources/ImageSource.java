package pipeline.sources;

import java.awt.image.BufferedImage;

import image.IImage;

public interface ImageSource {
	public abstract SourcedImage nextImage();
	
	public default IImage<?> nextIImage() {
		SourcedImage img = this.nextImage();
		return img == null ? null : img.unwrap();
	}

	public default BufferedImage nextBufferedImage() {
		SourcedImage img = this.nextImage();
		return img == null ? null : img.unwrapBufferedImage();
	}

	/**
	 * Once close() is called, release all references to objects, free all resources
	 * such as thread pools, open files and socket connections, and nextImage()
	 * methods should always return null.
	 * 
	 * However, if the ImageSource implemented has other ImageSources inside, it
	 * need not close them too. It must only give up references.
	 */
	public abstract void close();

}
