package app.actions;

import java.util.List;

import app.argparse.Options;
import hash.ImageHash;

@FunctionalInterface
public interface UnmatchedAction {
	abstract void exec(List<ImageHash> result, Options options);
}
