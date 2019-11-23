package attack;

import java.awt.image.BufferedImage;

import image.IImage;

public interface IAttack {

	abstract public IImage<?> attack(BufferedImage img);
	abstract public IImage<?> attack(IImage<?> img) throws UnsupportedOperationException;
}
