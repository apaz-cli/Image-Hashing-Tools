package pipeline.sources.ops;

import pipeline.sources.SourcedImage;

public interface SourcedImageOperation {
	public abstract SourcedImage operate(SourcedImage img);
}
