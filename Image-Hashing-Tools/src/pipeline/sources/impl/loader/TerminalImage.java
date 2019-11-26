package pipeline.sources.impl.loader;

import image.IImage;
import image.implementations.GreyscaleImage;
import pipeline.sources.SourcedImage;

class TerminalImage extends SourcedImage {

	public TerminalImage(IImage<?> img) {
		super(new GreyscaleImage(1, 1));
	}

}
