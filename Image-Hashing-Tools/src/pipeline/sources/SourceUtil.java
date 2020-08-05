package pipeline.sources;

import java.awt.image.BufferedImage;

import image.IImage;
import image.implementations.RGBAImage;
import image.implementations.SourcedImage;

public class SourceUtil {
	public static SourcedImage castToSourced(Object obj) {
		if (obj instanceof SourcedImage) {
			return (SourcedImage) obj;
		} else {
			throw new UnsupportedOperationException("Don't know how to cast type: " + obj.getClass().getName()
					+ " to SourcedImage. If necessary, please modify this method.");
		}
	}

	public static IImage<?> castToIImage(Object obj) {
		if (obj instanceof SourcedImage) {
			return ((SourcedImage) obj).unwrap();
		} else if (obj instanceof IImage<?>) {
			return (IImage<?>) obj;
		} else if (obj instanceof BufferedImage) {
			return new RGBAImage((BufferedImage) obj);
		} else {
			throw new UnsupportedOperationException("Don't know how to cast type: " + obj.getClass().getName()
					+ " to SourcedImage. If necessary, please modify this method.");
		}
	}

	public static BufferedImage castToBufferedImage(Object obj) {
		if (obj instanceof SourcedImage) {
			return ((SourcedImage) obj).unwrap().toBufferedImage();
		} else if (obj instanceof IImage<?>) {
			return ((IImage<?>) obj).toBufferedImage();
		} else if (obj instanceof BufferedImage) {
			return (BufferedImage) obj;
		} else {
			throw new UnsupportedOperationException("Don't know how to cast type: " + obj.getClass().getName()
					+ " to SourcedImage. If necessary, please modify this method.");
		}
	}
}
