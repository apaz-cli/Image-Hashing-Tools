package pipeline.sources;

import image.IImage;
import image.implementations.GreyscaleImage;

public class TerminalImage extends SourcedImage {

	public TerminalImage(IImage<?> img) {
		super(new GreyscaleImage(1, 1));
	}

	@Override
	public String toString() {
		return "TERMINALIMAGE";
	}
}
