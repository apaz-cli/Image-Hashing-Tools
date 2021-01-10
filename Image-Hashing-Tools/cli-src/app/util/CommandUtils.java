package app.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

import app.argparse.Options;
import image.IImage;

public class CommandUtils {
	private CommandUtils() {
	}

	protected void saveImage(Options options, IImage<?> img, File f, String format) throws IOException {
		if (options.verbose) System.out.println((f.exists() ? "Saving image: " : "Overwriting image: ") + f);
		if (options.touchDisk) ImageIO.write(img.toBufferedImage(), format, f);
	}

	protected void deleteImage(Options options, File f) {
		if (options.verbose) System.out.println("Deleting image: " + f);
		if (options.touchDisk) f.delete();
	}

	protected void moveImage(Options options, File from, File to) throws IOException {
		if (!from.exists()) {
			if (options.verbose) System.out.println("The file " + from + " was moved or deleted already.");
			return;
		}
		if (options.verbose)
			System.out.println("Moving image from " + from + " to " + to + (to.exists() ? " (Overwriting)" : ""));
		if (options.touchDisk) Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

}
