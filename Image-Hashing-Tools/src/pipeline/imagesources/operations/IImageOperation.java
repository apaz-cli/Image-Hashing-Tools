package pipeline.imagesources.operations;

import image.IImage;

public interface IImageOperation {
	public abstract IImage<?> operate(IImage<?> img);
}
