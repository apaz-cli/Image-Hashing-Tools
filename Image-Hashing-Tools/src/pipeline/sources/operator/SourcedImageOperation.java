package pipeline.sources.operator;

import pipeline.sources.SourcedImage;

public interface SourcedImageOperation extends ImageOperation {
	public abstract SourcedImage operate(SourcedImage img);
}
