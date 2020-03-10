package hashstore;

import java.io.File;
import java.util.List;

import hash.ComparisonType;
import hash.IHashAlgorithm;
import hash.ImageHash;
import hashstore.euclidean.EuclideanHyperplaneStore;
import pipeline.hasher.HasherOutput;

public interface HashStore extends HasherOutput {

	static HashStore build(File rootFolder, IHashAlgorithm alg) {

		ComparisonType t = alg.getComparisonType();
		switch (t) {
		case HAMMING:
			return new HammingStore();
		case EUCLIDEANF32:
			return new EuclideanHyperplaneStore();
		case EUCLIDEANF64:
			return new EuclideanHyperplaneStore();
		case EUCLIDEANI32:
			return new EuclideanHyperplaneStore();
		case OTHER:
			break;
		default:
			throw new UnsupportedOperationException("Unknown ComparisonType from alg.");
		}
		return null;
	}

	abstract File getRoot();

	// Finds the closest hash in the storage that isn't itself
	abstract ImageHash findNearest(ImageHash h);

	default String findNearestSource(ImageHash h) {
		return this.findNearest(h).getSource();
	}

	abstract boolean contains(ImageHash h);

	// Lists are sorted by similarity, with most similar first. Be careful not to
	// run out of memory with these.

	abstract List<ImageHash> findInRadius(ImageHash h);

	abstract List<ImageHash> toList();

	// Returns sources, not ImageHash objects.
	abstract List<String> findSourcesInRadius(ImageHash h);

	abstract List<String> toSourceList();

}
