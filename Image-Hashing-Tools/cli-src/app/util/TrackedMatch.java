package app.util;

import hash.ImageHash;
import pipeline.dedup.HashMatch;
import pipeline.sources.ImageLoader;

public class TrackedMatch extends HashMatch {

	private ImageLoader s1, s2;

	public TrackedMatch(ImageHash h1, ImageHash h2, ImageLoader s1, ImageLoader s2) {
		super(h1, h2);
		this.s1 = s1;
		this.s2 = s2;
	}

	public ImageLoader getFirstLoader() { return s1; }

	public ImageLoader getSecondLoader() { return s2; }
}
