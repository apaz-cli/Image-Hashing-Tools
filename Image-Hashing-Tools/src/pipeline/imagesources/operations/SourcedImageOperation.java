package pipeline.imagesources.operations;

import pipeline.imagesources.SourcedImage;

public interface SourcedImageOperation {
	public abstract SourcedImage operate(SourcedImage img);
}
