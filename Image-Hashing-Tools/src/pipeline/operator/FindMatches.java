package pipeline.operator;

import java.util.ArrayList;
import java.util.List;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import image.IImage;

public class FindMatches<T extends IImage<? extends T>> implements ImageOperation<T> {

	public FindMatches(IHashAlgorithm algorithm, ImageHash hash) {
		this(algorithm, MatchMode.NORMAL, hash);
	}

	public FindMatches(IHashAlgorithm algorithm, IImage<?> img) {
		this(algorithm, algorithm == null || img == null ? null : algorithm.hash(img));
	}

	public FindMatches(IHashAlgorithm algorithm, MatchMode mm, IImage<?> img) {
		this(algorithm, mm, algorithm.hash(img));
	}

	public FindMatches(IHashAlgorithm algorithm, MatchMode mm, ImageHash hash) {

		this.alg = algorithm;
		this.referenceHash = hash;
		this.mm = mm;
	}

	private IHashAlgorithm alg;
	private ImageHash referenceHash;
	private MatchMode mm;

	private List<ImageHash> matches = new ArrayList<>();

	public List<ImageHash> getMatches() {
		synchronized (this.matches) {
			return this.matches;
		}
	}

	public List<ImageHash> getAndClearMatches() {
		synchronized (this.matches) {
			List<ImageHash> l = this.matches;
			this.clearMatches();
			return l;
		}
	}

	public void clearMatches() {
		synchronized (this.matches) {
			this.matches = new ArrayList<ImageHash>();
		}
	}

	@Override
	public T apply(T img) {
		ImageHash h = alg.hash(img);
		if (alg.matches(this.referenceHash, h, this.mm)) {
			synchronized (this.matches) {
				this.matches.add(h);
			}
		}
		return img;
	}
}
