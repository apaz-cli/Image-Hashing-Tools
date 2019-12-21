package pipeline.sources;

import image.IImage;
import image.implementations.GreyscaleImage;

public class TerminalImage extends SourcedImage {

	public TerminalImage(IImage<?> img) {
		super(new GreyscaleImage(1, 1), "TERMINALIMAGE");
	}

	@Override
	public String toString() {
		return "TERMINALIMAGE";
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof TerminalImage;
	}
}
