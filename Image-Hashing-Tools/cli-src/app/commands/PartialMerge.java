package app.commands;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import hash.ImageHash;
import pipeline.ImageSource;
import pipeline.dedup.HashMatch;

class PartialMerge extends Command {

	HashResults hashResults = null;

	boolean sameSource;
	ImageSource[] sources;

	@Override
	public void runCommand() throws IOException {
		ImageSource[] sources = this.makeSources(1, 2);

		if (sources.length == 1) {
			this.hashResults = this.crossCompareSame(sources[0]);
		} else {
			this.hashResults = this.crossCompareDifferent(sources[0], sources[1]);
		}

		if (sources.length == 1) {
			mergeDuplicates(sources[0], null);
		} else {
			mergeDuplicates(sources[0], sources[1]);
		}

		return;
	}

	private void mergeDuplicates(final ImageSource from, final ImageSource to) {
		// We asserted above that from and to are the same type, either of ImageLoader
		// or URLDownloader

		// Delete exact duplicate images
		List<HashMatch> exactDuplicates = hashResults.getExactMatches();
		List<ImageHash> toDelete = exactDuplicates.stream().map(m -> {
			String s1 = m.getFirst().getSource();
			String s2 = m.getSecond().getSource();
			return this.same ? (s1.length() < s2.length() ? m.getSecond() : m.getFirst()) : m.getSecond();
		}).collect(Collectors.toList());
		this.deleteExact(toDelete, from);
		exactDuplicates.clear();

		// Move non-duplicate images with no matches if not same source (If not, they
		// don't need to be moved)
		List<ImageHash> toMove = hashResults.getNonMatched();
		if (!same) {
			this.moveUnmatched(toMove, from, to);
		}
		toMove.clear();

		// Leave possible duplicates. If the command that was called was actually
		// GuidedMerge, we'll use those once this command finishes. Otherwise, we don't
		// have to worry about freeing the memory, because the program is done anyway.
	}

}
