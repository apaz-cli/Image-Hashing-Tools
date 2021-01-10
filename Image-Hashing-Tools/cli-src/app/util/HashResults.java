package app.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import app.argparse.Options;
import hash.ImageHash;
import image.IImage;
import pipeline.sources.ImageLoader;
import utils.Pair;
import utils.Triple;

/**
 * The meanings of the lists are as follows:
 * 
 * 1. Exact Matches 2. Partial Matches 3. Non-matches
 * 
 * @author apaz
 */
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

		// Consume each source, hashing all the images.
		List<Pair<ImageLoader, List<ImageHash>>> sourceHashes = new ArrayList<>();
		for (ImageLoader source : sources) {
			List<ImageHash> hashedSource = source.parallelStreamHashes(options.algorithm).collect(Collectors.toList());
			sourceHashes.add(new Pair<>(source, hashedSource));
		}

		final List<TrackedMatch> exactMatches = new Vector<>();
		final List<TrackedMatch> partialMatches = new Vector<>();
		final List<ImageHash> nonMatches;
		final Set<ImageHash> matched = Collections.synchronizedSet(new HashSet<>());

		// Compare every source to itself if requested
		if (options.crossCompareSelf) {
			sourceHashes.parallelStream().forEach(hashList -> {
				// Staircase nested loops
				ImageLoader source = hashList.getKey();
				ImageHash[] hashes = (ImageHash[]) hashList.getValue().toArray();
				for (int i = 0; i < hashes.length; i++) {
					for (int j = i + 1; j < hashes.length; j++) {
						ImageHash h1 = hashes[i];
						ImageHash h2 = hashes[j];
						compareHashes(h1, h2, source, source, exactMatches, partialMatches, matched);
					}
				}
			});
		}

		// Compare each source to every other source.
		// First we make a list of every such combination of sources to consider, then
		// we do the comparison.
		if (options.crossCompareOthers) {
			// Staircase nested loops
			List<Pair<Pair<ImageLoader, List<ImageHash>>, Pair<ImageLoader, List<ImageHash>>>> sourcesToCompare = new ArrayList<>();
			for (int i = 0; i < sourceHashes.size(); i++) {
				for (int j = i + 1; j < sourceHashes.size(); j++) {
					sourcesToCompare.add(new Pair<>(sourceHashes.get(i), sourceHashes.get(j)));
				}
			}

			sourcesToCompare.parallelStream().forEach(imageSources -> {
				// Unpack
				Pair<ImageLoader, List<ImageHash>> p1 = imageSources.getKey(), p2 = imageSources.getValue();
				ImageLoader l1 = p1.getKey();
				ImageLoader l2 = p2.getKey();
				ImageHash[] hashes1 = (ImageHash[]) p1.getValue().toArray();
				ImageHash[] hashes2 = (ImageHash[]) p2.getValue().toArray();

				// Full nested loops
				for (int i = 0; i < hashes1.length; i++) {
					for (int j = 0; j < hashes2.length; j++) {
						compareHashes(hashes1[i], hashes2[j], l1, l2, exactMatches, partialMatches, matched);
					}
				}
			});
		}

		// Now that we've done all the comparisons, calculate from the set that was
		// matched which images were not matched.
		nonMatches = sourceHashes.stream().map(pair -> pair.getValue()).flatMap(hashList -> hashList.parallelStream())
				.filter(hash -> !matched.contains(hash)).collect(Collectors.toList());

		return new HashResults(exactMatches, partialMatches, nonMatches);
	}

	private static void compareHashes(ImageHash h1, ImageHash h2, ImageLoader l1, ImageLoader l2,
			List<TrackedMatch> exactMatches, List<TrackedMatch> partialMatches, Set<ImageHash> matched) {
		IImage<?> img1 = null, img2 = null;
		try {
			img1 = h1.loadFromSource();
			img2 = h2.loadFromSource();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (img1.equals(img2)) {
			exactMatches.add(new TrackedMatch(h1, h2, l1, l2));
			matched.add(h1);
			matched.add(h2);
		} else if (h1.getAlgorithm().matches(img1, img2)) {
			partialMatches.add(new TrackedMatch(h1, h2, l1, l2));
			matched.add(h1);
			matched.add(h2);
		}
	}

}
