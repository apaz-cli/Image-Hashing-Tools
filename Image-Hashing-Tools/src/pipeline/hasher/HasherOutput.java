package pipeline.hasher;

import hash.ImageHash;

public interface HasherOutput {
	public abstract void output(ImageHash hash);
}
