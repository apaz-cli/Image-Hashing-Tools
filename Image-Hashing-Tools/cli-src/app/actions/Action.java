package app.actions;

import java.util.List;
import app.argparse.Options;
import app.util.TrackedMatch;

@FunctionalInterface
public interface Action {
	abstract void exec(List<TrackedMatch> result, Options options);
}
