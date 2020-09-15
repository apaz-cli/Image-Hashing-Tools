package app.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hash.ImageHash;
import pipeline.ImageSource;

class TrimExact extends app.commands.Command {

	@Override
	public void runCommand() throws IOException {

		ImageSource[] sources = this.makeSources(1, 2);

		HashResults results;
		List<ImageHash> hashList = null;
		if (sources.length == 1) {
			results = this.crossCompareSame(sources[0]);
		} else {
			results = this.crossCompareDifferent(sources[0], sources[1]);
		}

		Set<ImageHash> deleteSet = new HashSet<>();
		results.getExactMatches().stream().forEach(match -> {
			ImageHash h1 = match.getFirst(), h2 = match.getSecond();
			boolean c1 = deleteSet.contains(h1), c2 = deleteSet.contains(h2);

			// The result of this should be that for each graph of exact matches, only one
			// vertex is retained.
			boolean changed = false;
			if (c1) {
				deleteSet.add(h2);
				changed = true;
			}
			if (c2) {
				deleteSet.add(h1);
				changed = true;
			}

			if (!changed) {
				deleteSet.add(h2);
			}
		});
		hashList = new ArrayList<>(deleteSet);

		this.deleteExact(hashList, sources[0]);

		return;

	}

}
