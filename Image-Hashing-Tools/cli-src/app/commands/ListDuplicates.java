package app.commands;

import java.io.IOException;

import pipeline.ImageSource;
import pipeline.dedup.HashMatch;

class ListDuplicates extends Command {

	@Override
	public void runCommand() throws IOException {

		ImageSource[] sources = this.makeSources(1, 2);

		HashResults results;
		if (sources.length == 1) {
			results = this.crossCompareSame(sources[0]);
		} else {
			results = this.crossCompareDifferent(sources[0], sources[1]);
		}

		printDuplicates(results);

		return;
	}

	private static void printDuplicates(HashResults results) {
		System.out.println("Exact Duplicates:");
		for (HashMatch d : results.getExactMatches()) {
			System.out.println(d);
		}
		System.out.println("\nPossible Duplicates:");
		for (HashMatch d : results.getPossibleMatches()) {
			System.out.println(d);
		}
	}

}
