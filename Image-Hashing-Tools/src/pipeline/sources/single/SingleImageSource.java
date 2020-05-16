package pipeline.sources.single;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import image.IImage;
import image.implementations.SourcedImage;
import pipeline.ImageSource;

public class SingleImageSource implements ImageSource {

	private SourcedImage img;

	public SingleImageSource(SourcedImage img) {
		this.img = img;
	}

	public SingleImageSource(BufferedImage img, String source) {
		this(new SourcedImage(img, source));
	}

	public SingleImageSource(BufferedImage img, String source, Boolean isURL) {
		this(new SourcedImage(img, source, isURL));
	}

	public SingleImageSource(BufferedImage img, URL source) {
		this(new SourcedImage(img, source));
	}

	public SingleImageSource(BufferedImage img, File source) {
		this(new SourcedImage(img, source));
	}

	public SingleImageSource(IImage<?> img, String source) {
		this(new SourcedImage(img, source));
	}

	public SingleImageSource(IImage<?> img, String source, Boolean isURL) {
		this(new SourcedImage(img, source, isURL));
	}

	public SingleImageSource(IImage<?> img, URL source) {
		this(new SourcedImage(img, source));
	}

	public SingleImageSource(IImage<?> img, File source) {
		this(new SourcedImage(img, source));
	}

	@Override
	public synchronized SourcedImage nextImage() {
		SourcedImage prevImg = img;
		if (prevImg != null) {
			img = null;
		}
		return prevImg;
	}

	@Override
	public synchronized void close() {
		img = null;
	}

}
