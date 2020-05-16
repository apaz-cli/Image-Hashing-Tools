package hashstore;

import java.io.File;
import java.util.List;

import hash.IHashAlgorithm;
import hash.ImageHash;
import pipeline.hasher.HasherOutput;

public interface HashStore extends HasherOutput {

	static HashStore build(File rootFolder, IHashAlgorithm alg) {
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
	
	abstract void store(ImageHash hash) throws UnsupportedOperationException;
	
	@Override
	default void accept(ImageHash hash) throws UnsupportedOperationException {
		this.store(hash);
	}

}
