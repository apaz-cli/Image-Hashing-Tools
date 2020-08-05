package pipeline.operator;

import image.IImage;

@FunctionalInterface
public interface ImageOperation<T extends IImage<? extends T>> {
	public abstract T apply(T img);
}
