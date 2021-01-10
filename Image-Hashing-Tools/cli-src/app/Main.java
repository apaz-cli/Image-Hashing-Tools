package app;

import java.io.IOException;

import app.argparse.Options;
import app.util.HashResults;
import pipeline.sources.ImageLoader;

public class Main {

	// @nof
	public static String HELP = 
			"Usage: ihtools [options]... command [arguments]...\n" + 
			"Options:\n" + 
			"  -h, --help          Display this message and exit.\n" +
			"  -c, --commands      Display a help message detailing the usage of each command.\n" +
			"  -v, --verbose       Print all the files that are created, move or are deleted as a result of this program.\n" +
			"  -t, --test          Don't actually create, move, or be delete files. Best combined with --verbose.\n" + 
			"  -s, --same          Find hash matches within the same target folder. Must provide either -s and/or -o.\n" +
			"  -o, --other         Find hash matches within different target folders. Must provide either -s and/or -o.\n" +
			"  -p, --pHash <#>     Use the pHash algorithm for comparison, with the specified side length.\n" +
			"  -d, --dHash <#>     Use the dHash algorithm for comparison, with the specified side length.\n"+
			"  -a, --aHash <#>     Use the aHash algorithm for comparison, with the specified side length.\n";
	// @dof

	public static void main(String[] args) {
		// Print usage if no args
		if (args.length == 0) {
			System.out.println(HELP);
			System.exit(0);
		}

		// Stores global program state
		Options options = new Options(args);

		HashResults results = null;
		try {
			results = HashResults.get(options);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		options.exactMatchAction.exec(results.getExactMatches(), options);
		options.partialMatchAction.exec(results.getPartialMatches(), options);
		options.nonMatchAction.exec(results.getNonMatched(), options);

		return;
	}

}
