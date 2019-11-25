package pipeline.sources.impl;

import java.awt.image.BufferedImage;

import image.IImage;
import image.implementations.RGBAImage;
import pipeline.sources.SourcedImage;

public class SourceUtil {
	public static SourcedImage castToSourced(Object obj) {
		if (obj instanceof IImage<?>) {
			return new SourcedImage((IImage<?>) obj);
		} else if (obj instanceof SourcedImage) {
			return (SourcedImage) obj;
		} else if (obj instanceof BufferedImage) {
			return new SourcedImage((new RGBAImage((BufferedImage) obj)));
		} else {
			throw new UnsupportedOperationException("Don't know how to cast type: " + obj.getClass().getName()
					+ " to SourcedImage. If necessary, please modify this method.");
		}
	}
}
