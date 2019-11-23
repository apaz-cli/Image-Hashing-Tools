package pipeline.imagesources;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import image.IImage;

public class SingleImageSource implements ImageSource {

	private SourcedImage img;
	boolean exhausted = false;

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
	public SourcedImage nextImage() {
		boolean wasExhausted = exhausted;
		exhausted = true;

		SourcedImage prevImg = img;
		img = null;

		return wasExhausted ? null : prevImg;
	}

	@Override
	public void close() {

	}

}
