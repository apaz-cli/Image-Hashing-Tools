package hashstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import pipeline.dedup.HashMatch;
import utils.Pair;

public class ListHashStore implements HashStore {

	List<ImageHash> hashes = new Vector<>();

	public ListHashStore() {}

	@Override
	public void store(ImageHash hash) {
		Objects.nonNull(hash);
		hashes.add(hash);
	}

	@Override
	public ImageHash NN(ImageHash h) throws IOException {
		return hashes.parallelStream().map(hash -> new Pair<>(h, h.distance(hash)))
				.min((pair1, pair2) -> pair1.getValue().compareTo(pair2.getValue())).map(pair -> pair.getKey())
				.orElse(null);
	}

	@Override
	public List<ImageHash> kNN(ImageHash h, int k) throws IllegalArgumentException, NullPointerException {
		if (k < 0) throw new IllegalArgumentException("k cannot be negative.");
		Objects.nonNull(h);

		List<Pair<ImageHash, Double>> sorted = hashes.parallelStream().map(hash -> new Pair<>(h, h.distance(hash)))
				.sorted((pair1, pair2) -> pair1.getValue().compareTo(pair2.getValue())).collect(Collectors.toList());

		return new ArrayList<>(sorted.stream().map(p -> p.getKey()).collect(Collectors.toList())
				.subList(0, Math.min(k, sorted.size())));
	}

	@Override
	public List<ImageHash> allWithinDistance(ImageHash h, double distance) throws IOException {
		return hashes.parallelStream().map(hash -> new Pair<>(h, h.distance(hash)))
				.filter(pair -> pair.getValue() <= distance).map(pair -> pair.getKey()).collect(Collectors.toList());
	}

	@Override
	public List<ImageHash> toList() throws IOException { return new ArrayList<>(hashes); }

	@Override
	public void storeAll(Collection<? extends ImageHash> hashes) { this.hashes.addAll(hashes); }

	@Override
	public List<HashMatch> findMatches(MatchMode mode) {
		ImageHash[] hashes = this.hashes.toArray(new ImageHash[this.hashes.size()]);
		List<HashMatch> matches = new ArrayList<>();

		if (hashes.length == 0) return matches;
		IHashAlgorithm alg = hashes[0].getAlgorithm();

		for (int i = 0; i < hashes.length; ++i) {
			for (int j = i + 1; j < hashes.length; ++j) {
				ImageHash h1 = hashes[i], h2 = hashes[j];
				if (alg.matches(h1, h2, mode)) matches.add(new HashMatch(h1, h2));
			}
		}

		return matches;
	}

}
