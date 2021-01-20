package app;

import java.io.IOException;
import app.argparse.Options;
import app.util.HashResults;

public class Main {

	// @nof
	public static String HELP = 
			"Usage: ihtools [OPTION]... FOLDER1 [FOLDER2]\n" + 
			"Options:\n" + 
			"  -h, --help          Display this message and exit.\n" +
			"  -v, --verbose       Print all the files that are created, move or are deleted as a result of this program.\n" +
			"  -t, --test          Don't actually create, move, or be delete files. Best combined with --verbose.\n" + 
			"  -s, --same          Find hash matches within the same target folder. Must provide either -s and/or -o.\n" +
			"  -o, --other         Find hash matches within different target folders. Must provide either -s and/or -o.\n" +
			"  -p, --pHash <#>     Use the pHash algorithm for comparison, with the specified side length.\n" +
			"  -d, --dHash <#>     Use the dHash algorithm for comparison, with the specified side length.\n"+
			"  -a, --aHash <#>     Use the aHash algorithm for comparison, with the specified side length.\n";
	// @dof

	public static void main(String[] args) {
		// TODO Swap comments for release.
		// Print usage if no args
		/* if (args.length == 0) { System.out.println(HELP); System.exit(0); } */

		// Stores global program state
		// Options options = new Options(args);
		Options options = new Options(new String[] { "-v", "-t", "-s", "-p", "64", "-e", "deletebutone", "-c", "gui",
				"/media/apaz/b5643d45-4ebf-4908-a525-7c59c36339c2/The Good Stuff/New Downloads to Sort/2d/" });
		// Options options = Options.testingOptions();

		if (options.verbose) System.out.println("Finished making image sources, now hashing.");

		HashResults results = null;
		try {
			results = HashResults.get(options);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (options.verbose) {
			System.out.println("Exact matches found: " + results.getExactMatches().size());
			System.out.println("Partial matches found: " + results.getPartialMatches().size());
			System.out.println("Unmatched found: " + results.getNonMatched().size());
		}

		if (options.verbose) System.out.println("Finished comparing images, now executing exact action.");
		options.exactMatchAction.exec(results.getExactMatches(), options);

		if (options.verbose) System.out.println("Finished exact action, moving on to partial action.");
		options.partialMatchAction.exec(results.getPartialMatches(), options);

		if (options.verbose) System.out.println("Finished partial action, moving on to unmatched action.");
		options.nonMatchAction.exec(results.getNonMatched(), options);

		if (options.verbose) System.out.println("Finished everything, exiting.");
		return;
	}

}
