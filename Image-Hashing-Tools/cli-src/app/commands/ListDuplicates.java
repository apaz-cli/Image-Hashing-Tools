package app.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import pipeline.dedup.HashMatch;
import pipeline.sources.SavingImageSource;
import utils.Pair;

public class ListDuplicates extends Command {

	@Override
	public void runCommand() throws IOException {
		validateArgs(this.args);

		SavingImageSource fromSource = this.parseSource(args.get(0));

		{ // IF THE SOURCES ARE GOING TO BE THE SAME, OPTIMIZE BY NOT HASHING EACH SET
			boolean sameSource;
			try {
				sameSource = this.args.get(0).equals(this.args.get(1));
				sameSource = sameSource
						|| Files.isSameFile(new File(this.args.get(0)).toPath(), new File(this.args.get(1)).toPath());
			} catch (IOException e) {
				sameSource = false;
			}

			if (sameSource) {
				Pair<List<HashMatch>, List<HashMatch>> duplicates = compareSame(this.commandOptions.algorithm,
						fromSource);
				this.printDuplicates(duplicates);

				try {
					fromSource.close();
				} catch (IOException e) {
					System.out.println("Was unable to write any changes.");
					e.printStackTrace();
				}
				return;
			}
		}

		// OTHERWISE, MAKE THE OTHER TWO AND COMPARE THEM AS DIFFERENT
		SavingImageSource refSource = this.parseSource(args.get(1));

		Pair<List<HashMatch>, List<HashMatch>> duplicates = ListDuplicates
				.compareDifferent(this.commandOptions.algorithm, fromSource, refSource);

		this.printDuplicates(duplicates);

		try {
			fromSource.close();
			refSource.close();
		} catch (IOException e) {
			System.out.println("Was unable to write changes.");
			e.printStackTrace();
		}
		return;
	}

	public static void validateArgs(List<String> args) {
		if (args.size() == 1) {
			args.add(args.get(0));
		}
		if (args.size() > 3 || args.size() == 0) {
			System.out.println("Expected 1-2 image sources as command line arguments. Got: " + args);
			System.exit(0);
		}
	}

	private void printDuplicates(Pair<List<HashMatch>, List<HashMatch>> duplicates) {
		System.out.println("Exact Duplicates:");
		for (HashMatch d : duplicates.getKey()) {
			System.out.println(d);
		}
		System.out.println("\nPossible Duplicates:");
		for (HashMatch d : duplicates.getValue()) {
			System.out.println(d);
		}
	}

}
