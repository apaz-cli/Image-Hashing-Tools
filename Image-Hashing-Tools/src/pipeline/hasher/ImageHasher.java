
package pipeline.hasher;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import hash.IHashAlgorithm;
import image.IImage;
import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;
import pipeline.sources.impl.collection.ImageCollection;
import pipeline.sources.impl.loader.ImageLoader;

public class ImageHasher {

	private ImageSource source;
	private IHashAlgorithm algorithm;
	private HasherOutput outputLambda;

	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, HasherOutput outputLambda)
			throws IllegalArgumentException, IOException {
		this((Object) source, algorithm, outputLambda);
	}

	public ImageHasher(Object input, IHashAlgorithm algorithm, HasherOutput outputLambda)
			throws IllegalArgumentException, IOException {
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.outputLambda = outputLambda == null ? (hash) -> {
			System.out.println(hash);
		} : outputLambda;
	}

	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, PrintStream output)
			throws IllegalArgumentException, IOException {
		this((Object) source, algorithm, output);
	}

	public ImageHasher(Object input, IHashAlgorithm algorithm, PrintStream output)
			throws IllegalArgumentException, IOException {
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.outputLambda = output == null ? (hash) -> {
			System.out.println(hash);
		} : (hash) -> {
			output.println(hash);
		};
	}

	private ImageSource createSource(Object input) {
		// Create ImageSource out of input
		ImageSource src = null;
		if (input instanceof ImageSource) {
			src = (ImageSource) input;
		} else if (input instanceof File) {
			File s = (File) input;
			boolean readable = s.canRead();
			if (!readable) {
				throw new IllegalArgumentException("The source file must be a readable directory. Readable: " + readable
						+ ". If the file is not readable, this could possibly be because it does not exist, "
						+ "or may alternatively be due to insufficient permissions. "
						+ "If you want to read only one file, use ImageUtils.openImage.");
			}
			src = new ImageLoader(s);
		} else if (input instanceof Collection<?>) {
			src = new ImageCollection((Collection<?>) input);
		}
		return src;
	}

	public void hashUnsourced() {
		outputLambda.output(algorithm.hash(source.nextIImage()));
	}

	public void hash() {
		outputLambda.output(algorithm.hash(source.nextImage()));
	}

	public void hashAll() {
		SourcedImage img = null;
		for (; (img = source.nextImage()) != null;) {
			outputLambda.output(algorithm.hash(img));
		}
	}

	public void hashAllUnsourced() {
		IImage<?> img = null;
		for (; (img = source.nextIImage()) != null;) {
			outputLambda.output(algorithm.hash(img));
		}
	}

}
