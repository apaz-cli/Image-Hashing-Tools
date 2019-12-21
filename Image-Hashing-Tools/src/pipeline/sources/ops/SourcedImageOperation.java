package pipeline.sources.ops;

import pipeline.sources.SourcedImage;

public interface SourcedImageOperation extends ImageOperation {
	public abstract SourcedImage operate(SourcedImage img);
}
