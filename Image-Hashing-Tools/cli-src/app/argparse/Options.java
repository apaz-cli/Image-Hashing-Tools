package app.argparse;

import java.io.File;
import java.util.Arrays;

import app.Main;
import app.actions.Action;
import app.actions.Actions;
import app.actions.UnmatchedAction;
import hash.IHashAlgorithm;
import hash.MatchMode;
import hash.implementations.AverageHash;
import hash.implementations.DifferenceHash;
import hash.implementations.PerceptualHash;
import pipeline.sources.ImageLoader;

public class Options {

	// The Options object actually tracks the state of the whole application.

	public String[] targets;
	public ImageLoader[] targetSources;

	public boolean crossCompare = false;
	public IHashAlgorithm algorithm = new DifferenceHash();
	public boolean touchDisk = true;
	public boolean verbose = false;

	public Action exactMatchAction = Actions.noop;
	public Action partialMatchAction = Actions.noop;
	public UnmatchedAction nonMatchAction = Actions.unnoop;

	// For testing purposes only
	private Options() {
	}

	public static Options testingOptions() {
		Options ret = new Options();
		ret.targets = new String[] { "/home/apaz/Downloads" };
		ret.targetSources = new ImageLoader[] { new ImageLoader(ret.targets[0]) };
		ret.crossCompare = false;
		ret.touchDisk = false;
		ret.verbose = true;
		ret.exactMatchAction = Actions.print;
		ret.partialMatchAction = Actions.gui;
		ret.nonMatchAction = Actions.unnoop;
		return ret;
	}

	public Options(String[] args) {
		ArgParser parser = new ArgParser(args);

		if (parser.switchPresent(new Switch("-h", "--help"))) {
			System.out.println(Main.HELP);
			System.exit(0);
		}

		if (parser.switchPresent(new Switch("-v", "--verbose"))) this.verbose = true;
		if (parser.switchPresent(new Switch("-t", "--test"))) this.touchDisk = false;

		// Parse Actions from args
		if (parser.switchPresent(new Switch("-e", "--exact"))) {
			switch (parser.lastSwitchValue("noop").toLowerCase()) {
			case "deletebutone":
				exactMatchAction = Actions.deletebutone;
				break;
			case "deletecover":
				exactMatchAction = Actions.deletecover;
				break;
			case "print":
				exactMatchAction = Actions.print;
				break;
			default: // noop
				break;
			}
		}
		if (parser.switchPresent(new Switch("-c", "--partial"))) {
			switch (parser.lastSwitchValue("noop").toLowerCase()) {
			case "gui":
				partialMatchAction = Actions.gui;
				break;
			case "print":
				partialMatchAction = Actions.print;
				break;
			default: // noop
				break;
			}
		}
		if (parser.switchPresent(new Switch("-n", "--nonmatched"))) {
			switch (parser.lastSwitchValue("noop").toLowerCase()) {
			case "move":
				nonMatchAction = Actions.unmove;
				break;
			case "print":
				nonMatchAction = Actions.unprint;
				break;
			case "delete":
				nonMatchAction = Actions.undelete;
				break;
			default:
				nonMatchAction = Actions.unnoop;
				break;
			}
		}

		// Alg information
		MatchMode mode = MatchMode.NORMAL;
		if (parser.switchPresent(new Switch("-m", "--matchmode"))) {
			String mm = parser.lastSwitchValue("normal").toLowerCase();
			switch (mm) {
			case "exact":
				mode = MatchMode.EXACT;
				break;
			case "strict":
				mode = MatchMode.STRICT;
				break;
			case "sloppy":
				mode = MatchMode.SLOPPY;
				break;
			case "normal":
				mode = MatchMode.NORMAL;
				break;
			default:
				System.out.println("Expected MatchMode \"exact\", \"strict\", \"sloppy\", or \"normal\". Got: " + mm);
				System.exit(1);
			}
		}

		// Possibly pick up the next
		int defaultSideLength = 16;
		if (parser.switchPresent(new Switch("-p", "--phash"))) {
			this.algorithm = new PerceptualHash(parseSideLength(parser, defaultSideLength), mode);
		} else if (parser.switchPresent(new Switch("-d", "--dhash"))) {
			this.algorithm = new DifferenceHash(parseSideLength(parser, defaultSideLength), mode);
		} else if (parser.switchPresent(new Switch("-a", "--ahash"))) {
			this.algorithm = new AverageHash(parseSideLength(parser, defaultSideLength), mode);
		} else {
			this.algorithm = new DifferenceHash();
		}

		this.targets = parser.targets();

		// Set compare self or others
		if (this.targets.length == 1) {
			this.crossCompare = false;
		} else if (this.targets.length == 2) {
			this.crossCompare = true;
		} else {
			System.out.println("Expected 1-2 targets. Got: " + Arrays.toString(this.targets));
			System.exit(0);
		}

		if (this.verbose) System.out.println("Making image sources.");
		this.targetSources = makeSources(this.targets);

		if (this.verbose) {
			System.out.println("Options:");
			System.out.println("targets: " + Arrays.toString(targets));
			System.out.println("crossCompare: " + this.crossCompare);
			System.out.println("touchDisk: " + this.touchDisk);
			System.out.println("verbose: " + this.verbose);
		}

	}

	private int parseSideLength(ArgParser parser, int defaultSideLength) {
		String sideLength = parser.lastSwitchValue();
		if (sideLength == null) return defaultSideLength;
		int sl = defaultSideLength;
		try {
			sl = Integer.parseInt(sideLength);
			if (!(sl > 0)) throw new NumberFormatException();
		} catch (NumberFormatException e) {
			System.out.println(
					"Could not parse the side length the image hashing algorithm. Expected a positive number, but got: "
							+ sideLength);
			System.exit(1);
		}

		return sl;
	}

	private static ImageLoader[] makeSources(String[] targets) {
		if (targets.length == 0) {
			System.out.println("Must provide at least one target folder.");
			System.exit(0);
		}

		// Convert to files
		File[] targetFiles = new File[targets.length];
		for (int i = 0; i < targets.length; i++) {
			targetFiles[i] = new File(targets[i]);
		}

		// Assert that all the sources aren't equal to each other
		for (int i = 0; i < targetFiles.length; i++) {
			for (int j = i + 1; j < targetFiles.length; j++) {
				if (targetFiles[i].equals(targetFiles[j])) {
					System.out.println("Targets " + (i + 1) + " and " + (j + 1) + " point to the same folder.");
					System.exit(1);
				}
			}
		}

		// Construct sources
		ImageLoader[] loaders = new ImageLoader[targets.length];
		for (int i = 0; i < targets.length; i++) {
			try {
				loaders[i] = new ImageLoader(targets[i]);
			} catch (IllegalArgumentException e) {
				System.out
						.println("The targets provided must all be readable folders. \"" + targets[i] + "\" was not.");
				System.exit(1);
			}
		}

		// TODO Make sure none of the sources are inside of each other.

		return loaders;
	}

}
