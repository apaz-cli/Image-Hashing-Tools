package pipeline.sources.operator;

import image.IImage;

public interface IImageOperation extends ImageOperation {
	public abstract IImage<?> operate(IImage<?> img);
}
