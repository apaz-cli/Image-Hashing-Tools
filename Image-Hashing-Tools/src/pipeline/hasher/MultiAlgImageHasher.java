package pipeline.hasher;

import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import hash.IHashAlgorithm;
import hash.ImageHash;
import image.IImage;
import image.PixelUtils;
import pipeline.ImageSource;

public class MultiAlgImageHasher {

	/*
	 * The algorithm and HasherOutput are private so that MultiAlgImageHasher
	 * doesn't inherit it.
	 */
	protected ImageSource source;
	private IHashAlgorithm[] algorithms;
	private HasherOutput[] outputLambdas;

	public MultiAlgImageHasher(ImageSource source, List<IHashAlgorithm> algorithms, List<HasherOutput> outputLambdas)
			throws IllegalArgumentException {
		this((Object) source, algorithms, outputLambdas);
	}

	public MultiAlgImageHasher(Object input, List<IHashAlgorithm> algorithms, List<?> outputs)
			throws IllegalArgumentException {
		PixelUtils.assertNotNull(input, algorithms);
		int n;
		if ((n = algorithms.size()) != (outputs == null ? n : outputs.size()))
			throw new IllegalArgumentException("Lists of algorithms and outputs must be the same size.");
		this.source = ImageHasher.createSource(input);
		this.algorithms = algorithms.toArray(new IHashAlgorithm[n]);
		this.outputLambdas = createOutputs(outputs, n).toArray(new HasherOutput[n]);
	}

	/**
	 * Another helper method, with the same purpose as
	 * {@link ImageHasher#createSource(Object)}. This method creates a HasherOutput
	 * out of a bunch of different types of objects, so that we don't have to add a
	 * bunch of constructors.
	 * 
	 * This one however can accept null and return an empty lambda expression.
	 */
	protected static List<HasherOutput> createOutputs(List<?> output, int n) throws IllegalArgumentException {
		if (output == null) {
			List<HasherOutput> ret = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				ret.add((hash) -> {});
			}
			return ret;
		}

		try {
			return output.stream().map(l -> { return ImageHasher.createOutput(l); }).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalArgumentException("An output was not able to be converted to a HasherOutput.");
		}
	}

	public int numberOfAlgorithms() {
		return this.algorithms.length;
	}

	public ImageHash[] hash() {
		IImage<?> img = source.nextImage();
		if (img == null) {
			return null;
		}

		ImageHash[] h = new ImageHash[algorithms.length];
		for (int i = 0; i < algorithms.length; i++) {
			h[i] = algorithms[i].hash(img);
			outputLambdas[i].accept(h[i]);
		}

		return h;
	}

	public List<ImageHash[]> hash(int numberOfHashes) throws IllegalArgumentException {
		if (numberOfHashes < 0) {
			throw new IllegalArgumentException("Number of iterations cannot be less than zero.");
		}

		List<ImageHash[]> completedHashes = new ArrayList<>();
		boolean cont = true;
		for (int i = 0; cont && i < numberOfHashes; i++) {
			ImageHash[] hash = this.hash();
			if (hash == null) {
				cont = false;
			} else {
				completedHashes.add(hash);
			}
		}

		return completedHashes;
	}

	public List<ImageHash[]> hash(int numberOfHashes, int parallelismLevel) throws IllegalArgumentException {
		if (numberOfHashes < 0) {
			throw new IllegalArgumentException("Number of iterations cannot be less than zero.");
		}
		if (parallelismLevel < 1) {
			throw new IllegalArgumentException("Number of threads cannot be less than one.");
		}

		final int hashesPerThread = numberOfHashes / parallelismLevel;
		final int hashesNotCovered = numberOfHashes - (hashesPerThread * parallelismLevel);

		List<ImageHash[]> completedHashes = new Vector<>();

		ExecutorService threadpool = Executors.newWorkStealingPool(parallelismLevel);
		for (int i = 0; i < parallelismLevel; i++) {

			final boolean extra = i < hashesNotCovered;
			threadpool.execute(() -> {

				int thisThreadHashes = extra ? hashesPerThread + 1 : hashesPerThread;

				ImageHash[] hash;
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
			threadpool.shutdown();
			threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS); // Wait a long time
		} catch (InterruptedException e) {
		}

		return completedHashes;
	}

	public void hashAll(int parallelismLevel) throws IllegalArgumentException {
		if (parallelismLevel < 1) {
			throw new IllegalArgumentException("Number of threads cannot be less than one.");
		}

		ExecutorService threadpool = Executors.newWorkStealingPool(parallelismLevel);
		for (int i = 0; i < parallelismLevel; i++) {
			threadpool.execute(() -> {
				@SuppressWarnings("unused")
				ImageHash[] hash;
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
