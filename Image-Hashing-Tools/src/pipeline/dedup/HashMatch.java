package pipeline.dedup;

import java.io.IOException;

import hash.IHashAlgorithm;
import hash.ImageHash;
import image.implementations.SourcedImage;
import utils.Pair;

public class HashMatch {

	private ImageHash h1, h2;

	public HashMatch(ImageHash h1, ImageHash h2) {
		if (h1 == null || h2 == null) throw new IllegalArgumentException("Hashes of match cannot be null.");
		this.h1 = h1;
		this.h2 = h2;
	}

	public ImageHash getFirst() { return h1; }

	public ImageHash getSecond() { return h2; }

	public SourcedImage loadFirst() throws IOException {
		return h1.loadFromSource();
	}

	public SourcedImage loadSecond() throws IOException { return h2.loadFromSource(); }

	public IHashAlgorithm loadAlg() { return h1.getAlgorithm(); }

	@Override
	public String toString() { 
		return new StringBuilder("<").append(h1).append("|").append(h2).append(">").toString(); }

	public Pair<SourcedImage, SourcedImage> loadBoth() throws IOException {
		return new Pair<>(this.loadFirst(), this.loadSecond());
	}

}
