package app.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import pipeline.ImageSource;
import pipeline.dedup.HashMatch;
import pipeline.sources.SavingImageSource;
import utils.Pair;

public class TrimExact extends app.commands.Command {

	// Delete images in fromSource with an identical duplicate in refSource. Note
	// that fromSource and refSource can be the same Source.

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
				this.deleteExactDuplicates(duplicates.getKey(), fromSource, false);

				fromSource.close();
				return;
			}
		}

		// OTHERWISE, MAKE THE OTHER TWO AND COMPARE THEM AS DIFFERENT
		SavingImageSource refSource = this.parseSource(args.get(1));

		Pair<List<HashMatch>, List<HashMatch>> duplicates = compareDifferent(this.commandOptions.algorithm, fromSource,
				refSource);

		this.deleteExactDuplicates(duplicates.getKey(), fromSource, true);

		fromSource.close();
		refSource.close();
		return;

	}

	private void deleteExactDuplicates(List<HashMatch> exactMatches, ImageSource src, boolean deleteFirst) {

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

}
