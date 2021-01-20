package app.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import app.argparse.Options;
import hash.ImageHash;
import image.IImage;
import pipeline.sources.ImageLoader;
import utils.Triple;

public class HashResults extends Triple<List<TrackedMatch>, List<TrackedMatch>, List<ImageHash>> {

	private HashResults(List<TrackedMatch> first, List<TrackedMatch> second, List<ImageHash> third) {
		super(first, second, third);
	}

	public List<TrackedMatch> getExactMatches() { return this.getFirst(); }

	public List<TrackedMatch> getPartialMatches() { return this.getSecond(); }

	public List<ImageHash> getNonMatched() { return this.getThird(); }

	public static HashResults get(Options options) throws IOException {
		ImageLoader[] sources = options.targetSources;

		if (options == null || sources == null) throw new NullPointerException();
		if (sources.length == 0) throw new IllegalArgumentException("Must compare at least one image source.");

		final List<TrackedMatch> exactMatches = new Vector<>();
		final List<TrackedMatch> partialMatches = new Vector<>();
		final List<ImageHash> nonMatches;
		final Set<ImageHash> matched = Collections.synchronizedSet(new HashSet<>());

		if (!options.crossCompare) {
			if (options.verbose) System.out.println("Comparing within same source: " + options.targets[0]);
			if (options.verbose) System.out.println("Hashing the images.");

			final ImageLoader source = sources[0];
			final ImageHash[] hashes = (ImageHash[]) source.parallelStreamHashes(options.algorithm).collect(Collectors.toList())
					.toArray(new ImageHash[0]);

			if (options.verbose) System.out.println("Comparing hashes.");

			ExecutorService threadpool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() * 2);

			for (int i = 0; i < hashes.length; i++) {
				ImageHash h1 = hashes[i];

				final ImageHash[] subarr = Arrays.copyOfRange(hashes, i + 1, hashes.length);
				int sublen = subarr.length;
				if (sublen > 200) {
					threadpool.execute(() -> {
						for (ImageHash h2 : subarr)
							compareHashes(h1, h2, source, source, exactMatches, partialMatches, matched);
					});
				} else {
					for (ImageHash h2 : subarr)
						compareHashes(h1, h2, source, source, exactMatches, partialMatches, matched);
				}
			}

			try {
				threadpool.shutdown();
				threadpool.awaitTermination(2, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (options.verbose) System.out.println("Finished comparing hashes.");

			List<ImageHash> all = new ArrayList<>(List.of(hashes));
			nonMatches = all.stream().filter(hash -> !matched.contains(hash)).collect(Collectors.toList());
		}

		// Compare each source to every other source.
		// First we make a list of every such combination of sources to consider, then
		// we do the comparison.
		else {
			if (options.verbose) System.out.println(
					"Comparing within different sources: " + options.targets[0] + " and " + options.targets[1]);
			final ImageLoader l1 = options.targetSources[0];
			final ImageLoader l2 = options.targetSources[1];

			if (options.verbose) System.out.println("Hashing first source.");
			final List<ImageHash> hashes1 = sources[0].parallelStreamHashes(options.algorithm)
					.collect(Collectors.toList());

			if (options.verbose) System.out.println("Hashing second source.");
			final ImageHash[] hashes2 = sources[1].parallelStreamHashes(options.algorithm).collect(Collectors.toList())
					.toArray(new ImageHash[0]);

			if (options.verbose) System.out.println("Cross comparing hashes.");
			hashes1.parallelStream().forEach(h1 -> {
				for (ImageHash h2 : hashes2) compareHashes(h1, h2, l1, l2, exactMatches, partialMatches, matched);
			});

			if (options.verbose) System.out.println("Finished comparing hashes.");

			List<ImageHash> all = new ArrayList<>(hashes1);
			all.addAll(Arrays.asList(hashes2));
			if (options.verbose) System.out.println("Calculating non-matches.");
			nonMatches = all.stream().filter(hash -> !matched.contains(hash)).collect(Collectors.toList());
		}

		return new HashResults(exactMatches, partialMatches, nonMatches);
	}

	private static Semaphore loadSequential = new Semaphore(5);

	private static void compareHashes(ImageHash h1, ImageHash h2, ImageLoader l1, ImageLoader l2,
			List<TrackedMatch> exactMatches, List<TrackedMatch> partialMatches, Set<ImageHash> matched) {

		boolean matches = h1.getAlgorithm().matches(h1, h2);

		if (matches) {
			matched.add(h1);
			matched.add(h2);

			// Only keep two images loaded in memory at a time.
			try {
				loadSequential.acquire();

				IImage<?> img1 = null, img2 = null;
				try {
					img1 = h1.loadFromSource();
					img2 = h2.loadFromSource();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				if (img1.toRGBA().equals(img2.toRGBA())) { // check if exact
					exactMatches.add(new TrackedMatch(h1, h2, l1, l2));
				} else { // otherwise is partial
					partialMatches.add(new TrackedMatch(h1, h2, l1, l2));
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} finally {
				loadSequential.release();
			}
		} // otherwise is unmatched, which is the compliment of matched.
	}

}
