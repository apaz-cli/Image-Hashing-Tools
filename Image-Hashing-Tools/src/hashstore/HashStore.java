package hashstore;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import hash.ImageHash;
import pipeline.dedup.HashMatch;
import pipeline.hasher.HasherOutput;

public interface HashStore extends HasherOutput {

	// Finds the closest hash in the storage that isn't itself
	abstract ImageHash NN(ImageHash h) throws IOException;

	abstract List<ImageHash> kNN(ImageHash h, int k) throws IOException;

	default String NNSource(ImageHash h) throws IOException {
		ImageHash nn = this.NN(h);
		return nn != null ? nn.getSource() : null;
	}

	abstract List<ImageHash> allWithinDistance(ImageHash h, double distance) throws IOException;

	default List<String> allSourcesWithinDistance(ImageHash h, double distance) throws IOException {
		return this.allWithinDistance(h, distance).stream().map(hash -> hash.getSource()).filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	abstract List<ImageHash> toList() throws IOException;

	default List<String> toSourceList() throws IOException {
		return this.toList().stream().map(h -> h.getSource()).filter(Objects::nonNull).collect(Collectors.toList());
	}

	abstract void storeAll(Collection<? extends ImageHash> hashes);

	default List<HashMatch> findMatches() throws IOException {
		return this.findMatches(500);
	}

	abstract List<HashMatch> findMatches(int atATime) throws IOException;

}
