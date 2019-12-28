package pipeline.sources.ops;

import image.IImage;

public interface IImageOperation extends ImageOperation {
	public abstract IImage<?> operate(IImage<?> img);
}
