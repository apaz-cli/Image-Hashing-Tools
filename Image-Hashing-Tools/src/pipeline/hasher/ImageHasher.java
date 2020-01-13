
package pipeline.hasher;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import hash.IHashAlgorithm;
import hash.ImageHash;
import image.IImage;
import pipeline.sources.ImageSource;
import pipeline.sources.SourcedImage;
import pipeline.sources.impl.collection.ImageCollection;
import pipeline.sources.impl.loader.ImageLoader;

public class ImageHasher {

	private int threadNum = 3;

	private ImageSource source;
	private IHashAlgorithm algorithm;
	private HasherOutput outputLambda;

	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, HasherOutput outputLambda)
			throws IllegalArgumentException {
		this((Object) source, algorithm, outputLambda);
	}

	public ImageHasher(Object input, IHashAlgorithm algorithm, HasherOutput outputLambda)
			throws IllegalArgumentException {
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.outputLambda = outputLambda == null ? (hash) -> {
			System.out.println(hash);
		} : outputLambda;
	}

	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, PrintStream output)
			throws IllegalArgumentException {
		this((Object) source, algorithm, output);
	}

	public ImageHasher(Object input, IHashAlgorithm algorithm, PrintStream output) throws IllegalArgumentException {
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.outputLambda = output == null ? (hash) -> {
			System.out.println(hash);
		} : (hash) -> {
			output.println(hash);
		};
	}

	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, int threadNum, HasherOutput outputLambda)
			throws IllegalArgumentException {
		this((Object) source, algorithm, outputLambda);
		this.threadNum = threadNum;
	}

	public ImageHasher(Object input, IHashAlgorithm algorithm, int threadNum, HasherOutput outputLambda)
			throws IllegalArgumentException {
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.outputLambda = outputLambda == null ? (hash) -> {
			System.out.println(hash);
		} : outputLambda;
		this.threadNum = threadNum;
	}

	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, int threadNum, PrintStream output)
			throws IllegalArgumentException {
		this((Object) source, algorithm, output);
		this.threadNum = threadNum;
	}

	public ImageHasher(Object input, IHashAlgorithm algorithm, int threadNum, PrintStream output)
			throws IllegalArgumentException {
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.outputLambda = output == null ? (hash) -> {
			System.out.println(hash);
		} : (hash) -> {
			output.println(hash);
		};
		this.threadNum = threadNum;
	}

	private static ImageSource createSource(Object input) {
		// Create ImageSource out of input
		ImageSource src = null;
		if (input instanceof ImageSource) {
			src = (ImageSource) input;
		} else if (input instanceof File) {
			File s = (File) input;
			boolean readable = s.canRead();
			if (!readable) {
				throw new IllegalArgumentException("The source file must be a readable directory. "
						+ "If the file is not readable, this could possibly be because it does not exist, "
						+ "or may alternatively be due to insufficient permissions. \n"
						+ "If you want to read only one file, first load it with ImageUtils.openImage(), "
						+ "or use a SingleImageSource.");
			}
			src = new ImageLoader(s);
		} else if (input instanceof Collection<?>) {
			src = new ImageCollection((Collection<?>) input);
		}
		return src;
	}

	public ImageHash hashUnsourced() {
		IImage<?> img = source.nextIImage();
		if (img == null) {
			return null;
		} else {
			ImageHash h = algorithm.hash(img);
			outputLambda.output(h);
			return h;
		}
	}

	public ImageHash hash() {
		SourcedImage img = source.nextImage();
		if (img == null) {
			return null;
		} else {
			ImageHash h = algorithm.hash(img);
			outputLambda.output(h);
			return h;
		}
	}

	public void hashAll() {
		ExecutorService pool = Executors.newWorkStealingPool(this.threadNum);
		for (int i = 0; i < this.threadNum; i++) {
			pool.execute(() -> {
				@SuppressWarnings("unused")
				ImageHash hash;
				while ((hash = this.hash()) != null) {
				}
			});
		}

		try {
			pool.shutdown();
			pool.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		}
	}

	public void hashAllUnsourced() {
		ExecutorService pool = Executors.newWorkStealingPool(this.threadNum);
		for (int i = 0; i < this.threadNum; i++) {
			pool.execute(() -> {
				@SuppressWarnings("unused")
				ImageHash hash;
				while ((hash = this.hashUnsourced()) != null) {
				}
			});
		}

		try {
			pool.shutdown();
			pool.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		}
	}

}
