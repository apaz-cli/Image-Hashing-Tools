package pipeline.sources.operator;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;
import image.implementations.RGBAImage;
import image.implementations.SourcedImage;

public class FindMatches implements IImageOperation {

	public FindMatches(IHashAlgorithm algorithm, ImageHash hash) {
		this(algorithm, MatchMode.NORMAL, hash);
	}

	public FindMatches(IHashAlgorithm algorithm, IImage<?> img) {
		this(algorithm, algorithm.hash(img));
	}

	public FindMatches(IHashAlgorithm algorithm, BufferedImage img) {
		this(algorithm, new RGBAImage(img));
	}

	public FindMatches(IHashAlgorithm algorithm, SourcedImage img) {
		this(algorithm, img.unwrap());
	}

	public FindMatches(IHashAlgorithm algorithm, MatchMode mm, IImage<?> img) {
		this(algorithm, mm, algorithm.hash(img));
	}

	public FindMatches(IHashAlgorithm algorithm, MatchMode mm, BufferedImage img) {
		this(algorithm, mm, new RGBAImage(img));
	}

	public FindMatches(IHashAlgorithm algorithm, MatchMode mm, SourcedImage img) {
		this(algorithm, mm, img.unwrap());
	}

	public FindMatches(IHashAlgorithm algorithm, MatchMode mm, ImageHash hash) {
		if (hash.toString().split(",")[0] != alg.getHashName() || hash.getLength() != algorithm.getHashLength()) {
			throw new IllegalArgumentException(
					"The name and hash lengths of the hash must match those which would be produced by the algorithm.");
		}

		this.alg = algorithm;
		this.referenceHash = hash;
		this.mm = mm;
	}

	private IHashAlgorithm alg;
	private ImageHash referenceHash;
	private MatchMode mm;

	private HashSet<String> matches = new HashSet<>();

	public HashSet<String> getMatches() {
		synchronized (this.matches) {
			return this.matches;
		}
	}

	public HashSet<String> getAndClearMatches() {
		synchronized (this.matches) {
			return this.matches;
		}
	}

	public void clearMatches() {
		synchronized (this.matches) {
			this.matches = new HashSet<String>();
		}
	}

	@Override
	public IImage<?> operate(IImage<?> img) {
		if (!(img instanceof SourcedImage)) {
			throw new IllegalArgumentException("This Operation only accepts SourcedImages.");
		}
		SourcedImage si = (SourcedImage) img;

		ImageHash h = alg.hash(si);
		boolean match = alg.matches(this.referenceHash, h, this.mm);
		if (match) {
			synchronized (this.matches) {
				this.matches.add(si.getSource());
			}
		}
		return si;
	}
}
