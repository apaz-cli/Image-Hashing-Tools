package hashstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import pipeline.dedup.HashMatch;
import vptree.VPTree;

public class VPHashStore implements HashStore {

	IHashAlgorithm alg;
	VPTree<ImageHash> vpt = null;

	public VPHashStore() {
	}

	/**
	 * Calling this several times instead of storeAll() once will lead to horrendous
	 * performance. The backing VPTree is rebuilt every time.
	 */
	@Override
	public void store(ImageHash hash) {
		if (hash == null) throw new NullPointerException("hash was null.");

		// Wrap the algorithm in a closure
		final IHashAlgorithm alg = hash.getAlgorithm();
		class VPHashComparator implements java.util.function.BiFunction<ImageHash, ImageHash, Double> {
			@Override
			public Double apply(ImageHash h1, ImageHash h2) {
				return alg.distance(h1, h2);
			}
		}

		if (this.vpt == null) {
			this.vpt = new VPTree<>(Collections.singleton(hash), new VPHashComparator());
			return;
		}

		if (!this.alg.algEquals(alg)) {
			throw new IllegalArgumentException("Cannot go from a VPHashStrore of one algorithm type to another. "
					+ "The hash passed to this method came from a different algorithm. Please keep them seperate.");
		}

		ArrayList<ImageHash> allHashes = new ArrayList<>(Arrays.asList(this.vpt.getItems()));
		allHashes.add(hash);
		allHashes.trimToSize();

		this.vpt.close();

		this.vpt = new VPTree<>(allHashes, new VPHashComparator());
	}

	@Override
	public void storeAll(Collection<? extends ImageHash> hashes) {
		if (hashes.isEmpty()) return;
		ImageHash hash = hashes.iterator().next();

		// Wrap the algorithm in a closure
		final IHashAlgorithm alg = hash.getAlgorithm();
		class VPHashComparator implements java.util.function.BiFunction<ImageHash, ImageHash, Double> {
			@Override
			public Double apply(ImageHash h1, ImageHash h2) {
				return alg.distance(h1, h2);
			}
		}

		if (this.vpt == null) {
			@SuppressWarnings("unchecked")
			Collection<ImageHash> imageHashes = (Collection<ImageHash>) hashes;
			this.vpt = new VPTree<ImageHash>(imageHashes, new VPHashComparator());
			return;
		}

		for (ImageHash h : hashes) {
			if (!h.getAlgorithm().algEquals(this.alg)) {
				throw new IllegalArgumentException("Cannot go from a VPHashStrore of one algorithm type to another. "
						+ "At least one of the hashes passed to this method came from a different algorithm. Please keep them seperate.");
			}
		}

		ArrayList<ImageHash> allHashes = new ArrayList<>(Arrays.asList(this.vpt.getItems()));
		allHashes.addAll(hashes);
		allHashes.trimToSize();

		this.vpt.close();

		this.vpt = new VPTree<>(allHashes, new VPHashComparator());
	}

	@Override
	public List<HashMatch> findMatches(MatchMode mode) {
		if (this.vpt == null) {
			throw new IllegalStateException("This VPHashStore is empty. Please add some hashes to it first.");
		}

		ImageHash[] items = this.vpt.getItems();

		// Dumb n^2 search
		ArrayList<HashMatch> matches = new ArrayList<>();
		for (int i = 0; i < items.length; i++) {
			for (int j = i + 1; j < items.length; j++) {
				ImageHash h1 = items[i];
				ImageHash h2 = items[i];
				this.alg.matches(h1, h2, mode);
				matches.add(new HashMatch(h1, h2));
			}
		}

		return matches;
	}

	@Override
	public ImageHash NN(ImageHash h) throws IOException {
		return vpt.nn(h).item;
	}

	@Override
	public List<ImageHash> kNN(ImageHash h, int k) throws IOException {
		return vpt.knn(h, k).stream().map(e -> e.item).collect(Collectors.toList());
	}

	@Override
	public List<ImageHash> allWithinDistance(ImageHash h, double distance) throws IOException {
		return Arrays.asList(vpt.getItems()).parallelStream()
				.filter(hash -> h.distance(hash) <= distance)
				.collect(Collectors.toList());
	}

	@Override	
	public List<ImageHash> toList() throws IOException {
		return Arrays.asList(vpt.getItems());
	}

}
