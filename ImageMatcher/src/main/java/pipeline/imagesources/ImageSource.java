package pipeline.imagesources;

import java.awt.image.BufferedImage;

public interface ImageSource {
	public abstract SourcedImage nextImage();
	public default BufferedImage nextBufferedImage() {
		SourcedImage img = this.nextImage();
		return img == null ? null : img.unwrap();
	}
	public abstract void close();
}
