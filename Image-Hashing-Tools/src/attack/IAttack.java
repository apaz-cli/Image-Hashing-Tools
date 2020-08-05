package attack;

import image.IImage;
import image.implementations.GreyscaleImage;
import pipeline.operator.ImageOperation;

@FunctionalInterface
public interface IAttack<T extends IImage<? extends T>> extends ImageOperation<T> {

	abstract public GreyscaleImage applyToChannel(GreyscaleImage channel);

	@SuppressWarnings("unchecked")
	@Override
	default public T apply(T img) {
		IImage<T> image = ((IImage<T>) img);
		return image.apply(this);
	}

}
