package attack;

import java.awt.image.BufferedImage;

import image.IImage;
import image.implementations.GreyscaleImage;
import image.implementations.RGBAImage;
import image.implementations.SourcedImage;
import pipeline.sources.operator.IImageOperation;

@FunctionalInterface
public interface IAttack extends IImageOperation {

	@Override
	default public IImage<?> operate(IImage<?> img) {
		return this.applyTo(img);
	}

	abstract public GreyscaleImage applyToColorChannel(GreyscaleImage channel);

	default public GreyscaleImage[] attackColorChannels(GreyscaleImage[] channels) {
		GreyscaleImage[] attacked = new GreyscaleImage[channels.length];
		for (int i = 0; i < channels.length; i++) {
			attacked[i] = this.applyToColorChannel(channels[i]);
		}
		return attacked;
	}

	default public IImage<?> applyTo(BufferedImage img) {
		return this.applyTo(new RGBAImage(img));
	}

	// If the attack is not meant to attack all color channels the same, or do
	// something in a specific colorspace, then the method below should be
	// overwritten.

	default public IImage<?> applyTo(IImage<?> img) {
		IImage<?> ret = null;
		if (img instanceof SourcedImage) {
			SourcedImage s = (SourcedImage) img;
			ret = new SourcedImage(this.applyTo(s.unwrap()), s.getSource(), s.isURL());
		} else {
			try {
				// If this exploded and you're reading the stack trace, you're going to have to
				// either add a constructor to your custom image class that takes an array of
				// Greyscale images, each of which the Attack is going to be applied to, or
				// otherwise you need to handle it yourself above right here in an else if.
				ret = img.getClass().getConstructor(GreyscaleImage[].class)
						.newInstance(new Object[] {this.attackColorChannels(img.getChannels())});
			} catch (Exception e) {
				System.err.println("THERE HAS BEEN A PROBLEM, AND THIS METHOD HAS BROKEN. "
						+ "PLEASE CREATE AN ISSUE ON GITHUB WITH THE FOLLOWING STACK TRACE: ");
				e.printStackTrace();
				System.exit(2);
			}
		}
		return ret;
	}

}
