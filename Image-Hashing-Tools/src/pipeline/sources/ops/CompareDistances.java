package pipeline.sources.ops;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

import hash.IHashAlgorithm;
import hash.ImageHash;
import image.IImage;
import image.implementations.RGBAImage;
import pipeline.sources.SourcedImage;

public class CompareDistances implements SourcedImageOperation {

	public CompareDistances(IHashAlgorithm algorithm, IImage<?> img) {
		this.alg = algorithm;
		this.referenceHash = algorithm.hash(img);
	}

	public CompareDistances(IHashAlgorithm algorithm, BufferedImage img) {
		this(algorithm, new RGBAImage(img));
	}

	public CompareDistances(IHashAlgorithm algorithm, SourcedImage img) {
		this(algorithm, img.unwrap());
	}

	private IHashAlgorithm alg;
	private ImageHash referenceHash;

	private List<Pair<String, Integer>> history = new ArrayList<>();

	@Override
	public SourcedImage operate(SourcedImage img) {
		ImageHash h = alg.hash(img);
		int distance = referenceHash.hammingDistance(h);
		Pair<String, Integer> p = new Pair<String, Integer>(h.getSource(), distance);
		synchronized (this.history) {
			this.history.add(p);
		}

		return img;
	}

	public List<Pair<String, Integer>> getHistory() {
		synchronized (this.history) {
			return this.history;
		}
	}

	// May run out of memory for large sets.
	public List<Pair<String, Float>> getHistoryPercentDifferences() {
		List<Pair<String, Float>> percentList = new ArrayList<>();

		float bitLength = (float) this.referenceHash.getLength();
		synchronized (this.history) {
			for (Pair<String, Integer> p : this.history) {
				float percentDifference = p.getValue() / bitLength;
				percentList.add(new Pair<String, Float>(p.getKey(), percentDifference));
			}
		}

		return percentList;
	}
}
