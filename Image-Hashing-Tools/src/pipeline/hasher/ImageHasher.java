
package pipeline.hasher;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hashstore.HashStore;
import image.IImage;
import image.PixelUtils;
import pipeline.ImageSource;
import pipeline.sources.ImageLoader;

/**
 * This class takes {@link IImage}s from an {@link ImageSource}, hashes them
 * with an {@link IHashAlgorithm}, then passes the resulting {@link ImageHash}es
 * to an output. That output can be absolutely whatever you want, and do
 * whatever you want, just write a HasherOutput lambda expression.
 * 
 * The input to the hasher can also largely be whatever you want, just write an
 * ImageSource. This is made more difficult by two guarantees that ImageSources
 * have to make. They must be thread-safe, and when they run out of images they
 * must always return null. However, once this is done, you'll have a fully
 * multithreaded solution for hashing them and reporting the results, as well as
 * benchmarking the success rate for hash retrieval after some modification to
 * the image such as a crop, flip, rotation or noise.
 * 
 * @author apaz-cli
 */
public class ImageHasher {

	/*
	 * The algorithm and HasherOutput are private so that MultiAlgImageHasher
	 * doesn't inherit it.
	 */
	protected ImageSource source;
	private IHashAlgorithm algorithm;
	private HasherOutput outputLambda;

	/**
	 * Creates an {@link ImageHasher} with the following properties. When
	 * {@link ImageHasher#hash()} or one of its derivatives are called, images are
	 * pulled from the {@link ImageSource} and hashed by the {@link IHashAlgorithm},
	 * then passed to the {@link HasherOutput}.
	 * 
	 * @param source       The ImageSource to hash the images from
	 * @param algorithm    The hash algorithm to use on the images
	 * @param outputLambda A lambda expression or other HasherOutput object to
	 *                     accept() the hashes that come out when an image is
	 *                     hashed. If outputLambda is null, a new one will be
	 *                     constructed and used that does nothing.
	 * @throws IllegalArgumentException When source or algorithm are null
	 * @author apaz-cli
	 */
	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, HasherOutput outputLambda)
			throws IllegalArgumentException {
		this((Object) source, algorithm, outputLambda);
	}

	/**
	 * This constructor tries its best to create an ImageHasher with the input and
	 * output objects provided, and throws an IllegalArgumentException if that's not
	 * possible, with a description of what went wrong.
	 * 
	 * When {@link ImageHasher#hash()} or one of its derivatives are called, images
	 * are pulled from the input, hashed with the algorithm, and passed to the
	 * Output.
	 * 
	 * If you would like to see a type of object supported that currently isn't,
	 * please contact me at <a href=
	 * "https://github.com/Aaron-Pazdera">https://github.com/Aaron-Pazdera</a> or
	 * submit a pull request.
	 * 
	 * @param input
	 * @param algorithm
	 * @param output
	 * @throws IllegalArgumentException When input or algorithm are null, input is
	 *                                  not an accepted object or output is not an
	 *                                  accepted object.
	 * @author apaz-cli
	 */
	public ImageHasher(Object input, IHashAlgorithm algorithm, Object output) throws IllegalArgumentException {
		PixelUtils.assertNotNull(input, algorithm);
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.outputLambda = createOutput(output);

	}

	/**
	 * A helper method that takes the input object from the constructor and creates
	 * an {@link ImageSource} out of it. This is so that the constructor can be
	 * expanded to multiple other types of Objects in the future when we can
	 * construct ImageSources out of them, all while not actually having to add a
	 * lot more constructors.
	 */
	static ImageSource createSource(Object input) throws IllegalArgumentException {
		if (input instanceof ImageSource) {
			return (ImageSource) input;
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
			return new ImageLoader(s);
		}
		throw new IllegalArgumentException("Expected an ImageSource or File for input to ImageHasher, but got: " + input.getClass());
	}

	/**
	 * Another helper method, with the same purpose as
	 * {@link ImageHasher#createSource(Object)}. This method creates a HasherOutput
	 * out of a bunch of different types of objects, so that we don't have to add a
	 * bunch of constructors.
	 * 
	 * This one however can accept null and return an empty lambda expression.
	 */
	@SuppressWarnings("unchecked")
	static HasherOutput createOutput(Object output) throws IllegalArgumentException {
		if (output == null) {
			return (hash) -> {};
		} else if (output instanceof HasherOutput) {
			return (HasherOutput) output;
		} else if (output instanceof PrintStream) {
			return (hash) -> { ((PrintStream) output).println(); };
		} else if (output instanceof PrintWriter) {
			return (hash) -> { ((PrintWriter) output).println(); };
		} else if (output instanceof HashStore) {
			return (hash) -> { ((HashStore) output).store(hash); };
		} else if (output instanceof Collection<?>) {
			try {
				return (hash) -> { ((Collection<ImageHash>) output).add(hash); };
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Expected a collection of ImageHash, but got a collection of some other type.");
			}
		}
		System.err.println("Could not discern the type of hasher output. Using no output. Got: " + output.getClass());
		return null;
	}

	/**
	 * Hashes an image from this {@link ImageHasher}'s input, sending the
	 * {@link ImageHash}es to this ImageHasher's output and also returning the hash.
	 * 
	 * @return hash The hash of an image from this ImageHasher's
	 *         {@link ImageSource}. If the ImageSource is out of images, then this
	 *         method returns null.
	 * @author apaz-cli
	 */
	public ImageHash hash() {
		IImage<?> img = source.next();
		if (img == null) { return null; }
		ImageHash h = algorithm.hash(img);
		outputLambda.store(h);
		return h;
	}

	/**
	 * Hashes a number of images from this {@link ImageHasher}'s input, sending the
	 * {@link ImageHash}es to this ImageHasher's output and returning all of the
	 * hashes in a list.
	 * 
	 * For a large number of hashes, use {@link ImageHasher#hashAll(int)} to consume
	 * less memory storing references inside a list, or if you don't necessarily
	 * want to consume the entire ImageSource, call this method multiple times so
	 * that the lists can be garbage collected.
	 * 
	 * @param numberOfHashes The number of images to take from this ImageHasher's
	 *                       {@link ImageSource} and hash.
	 * @return hashList The list of hashes that were completed. The size of this
	 *         list may be less than the number of hashes specified if this
	 *         ImageHasher's ImageSource did not contain that many images that could
	 *         be processed.
	 * @throws IllegalArgumentException When numberOfHashes is less than zero.
	 * @author apaz-cli
	 */
	public List<ImageHash> hash(int numberOfHashes) throws IllegalArgumentException {
		if (numberOfHashes < 0) {
			throw new IllegalArgumentException("Number of iterations cannot be less than zero.");
		}

		List<ImageHash> completedHashes = new ArrayList<ImageHash>();
		boolean cont = true;
		for (int i = 0; cont && i < numberOfHashes; i++) {
			ImageHash hash = this.hash();
			if (hash == null) {
				cont = false;
			} else {
				completedHashes.add(hash);
			}
		}

		return completedHashes;
	}

	/**
	 * Hashes a number of images from this {@link ImageHasher}'s input, sending the
	 * {@link ImageHash}es to this ImageHasher's output and returning all of the
	 * hashes in a list. The hashes and image loads are performed on a threadpool
	 * with at most the parallelismLevel many threads.
	 * 
	 * For a large number of hashes, use {@link ImageHasher#hashAll(int)} to consume
	 * less memory storing references inside a list, or if you don't necessarily
	 * want to consume the entire ImageSource, call this method multiple times so
	 * that the lists can be garbage collected.
	 * 
	 * @param numberOfHashes   The number of images to take from this ImageHasher's
	 *                         {@link ImageSource} and hash.
	 * @param parallelismLevel The maximum number of threads to put to work hashing
	 *                         items from the input to this ImageHasher
	 * @return hashList The list of hashes that were completed. The size of this
	 *         list may be less than the number of hashes specified if this
	 *         ImageHasher's ImageSource did not contain that many images that could
	 *         be processed.
	 * @throws IllegalArgumentException When numberOfHashes is less than zero or
	 *                                  parallelismLevel is less than one.
	 * @author apaz-cli
	 */
	public List<ImageHash> hash(int numberOfHashes, int parallelismLevel) throws IllegalArgumentException {
		if (numberOfHashes < 0) {
			throw new IllegalArgumentException("Number of iterations cannot be less than zero.");
		}
		if (parallelismLevel < 1) { throw new IllegalArgumentException("Number of threads cannot be less than one."); }

		final int hashesPerThread = numberOfHashes / parallelismLevel;
		final int hashesNotCovered = numberOfHashes - (hashesPerThread * parallelismLevel);

		List<ImageHash> completedHashes = new Vector<>();

		ExecutorService threadpool = Executors.newWorkStealingPool(parallelismLevel);
		for (int i = 0; i < parallelismLevel; i++) {

			final boolean extra = i < hashesNotCovered;
			threadpool.execute(() -> {

				int thisThreadHashes = extra ? hashesPerThread + 1 : hashesPerThread;

				ImageHash hash;
				boolean cont = true;
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
			/*
			 * We can expect some of these tasks to take a long time. The SafeBooruScraper
			 * ImageSource for example will last at least multiple days, depending on
			 * download speed. We can't say wait forever, so just say a very long time.
			 */
			threadpool.shutdown();
			threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		}

		return completedHashes;
	}

	/**
	 * Hashes until this {@link ImageHasher}'s input is completely consumed, sending
	 * the {@link ImageHash}es to this ImageHasher's output. The hashes are
	 * performed on a threadpool with at most parallelismLevel many threads.
	 * 
	 * @param parallelismLevel The maximum number of threads to put to work hashing
	 *                         items from the input to this ImageHasher
	 * @throws IllegalArgumentException When parallelismLevel is less than one.
	 * @author apaz-cli
	 */
	public void hashAll(int parallelismLevel) throws IllegalArgumentException {
		if (parallelismLevel < 1) { throw new IllegalArgumentException("Number of threads cannot be less than one."); }

		ExecutorService threadpool = Executors.newWorkStealingPool(parallelismLevel);
		for (int i = 0; i < parallelismLevel; i++) {
			threadpool.execute(() -> {
				@SuppressWarnings("unused")
				ImageHash hash;
				while ((hash = this.hash()) != null) {}
			});
		}

		try {
			threadpool.shutdown();
			threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		}
	}

}
