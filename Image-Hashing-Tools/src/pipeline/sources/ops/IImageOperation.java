package pipeline.sources.ops;

import image.IImage;

public interface IImageOperation {
	public abstract IImage<?> operate(IImage<?> img);
}
