
package pipeline.hasher;

import java.io.File;
import java.io.PrintStream;
import java.util.Vector;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import hash.IHashAlgorithm;
import hash.ImageHash;
import image.IImage;
import image.PixelUtils;
import image.implementations.SourcedImage;
import pipeline.sources.ImageSource;
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
		PixelUtils.assertNotNull(input, algorithm, outputLambda);
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.outputLambda = outputLambda;
	}

	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, PrintStream output)
			throws IllegalArgumentException {
		this((Object) source, algorithm, output);
	}

	public ImageHasher(Object input, IHashAlgorithm algorithm, PrintStream output) throws IllegalArgumentException {
		PixelUtils.assertNotNull(input, algorithm, output);
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.outputLambda = (hash) -> {
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
		PixelUtils.assertNotNull(input, algorithm, outputLambda);
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.outputLambda = outputLambda;
		this.threadNum = threadNum;
	}

	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, int threadNum, PrintStream output)
			throws IllegalArgumentException {
		this((Object) source, algorithm, output);
		this.threadNum = threadNum;
	}

	public ImageHasher(Object input, IHashAlgorithm algorithm, int threadNum, PrintStream output)
			throws IllegalArgumentException {
		PixelUtils.assertNotNull(input, algorithm, output);
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.threadNum = threadNum;
	}

	private static ImageSource createSource(Object input) {
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

		if (src == null) {
			throw new IllegalArgumentException(
					"Expected an ImageSource, File, or Collection<?> for input, but got: " + input.getClass());
		}
		return src;
	}

	public ImageHash hash() {
		IImage<?> img = source.nextImage();
		if (img == null) {
			return null;
		}
		ImageHash h = img instanceof SourcedImage ? algorithm.hash((SourcedImage) img) : algorithm.hash(img);
		outputLambda.accept(h);
		return h;
	}

	public List<ImageHash> hash(int iterations) {
		final int hashesPerThread = iterations / this.threadNum;
		final int hashesNotCovered = iterations - (hashesPerThread * this.threadNum);

		List<ImageHash> completedHashes = new Vector<>();

		ExecutorService threadpool = Executors.newWorkStealingPool(this.threadNum);
		for (int i = 0; i < this.threadNum; i++) {

			final boolean extra = i < hashesNotCovered;
			threadpool.execute(() -> {

				int thisThreadHashes = extra ? hashesPerThread + 1 : hashesPerThread;
				boolean cont = true;

				ImageHash hash;
				for (int it = 0; cont && it < thisThreadHashes; it++) {
					hash = this.hash();
					if (hash == null) {
						cont = false;
					} else {
						completedHashes.add(hash);
					}
				}
			});
		}

		try {
			// I need to wait for some amount of time, I can't specify forever. But, we can
			// expect some of these tasks to take a long time. The SafeBooruScraper
			// ImageSource for example will last at least multiple days, depending on
			// download speed.
			threadpool.shutdown();
			threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		}

		return completedHashes;
	}

	public void hashAll() {
		ExecutorService threadpool = Executors.newWorkStealingPool(this.threadNum);
		for (int i = 0; i < this.threadNum; i++) {
			threadpool.execute(() -> {
				@SuppressWarnings("unused")
				ImageHash hash;
				while ((hash = this.hash()) != null) {
				}
			});
		}

		try {
			threadpool.shutdown();
			threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		}
	}

}
