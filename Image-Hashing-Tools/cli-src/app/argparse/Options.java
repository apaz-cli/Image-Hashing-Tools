package app.argparse;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

	// The options object (singleton) actually tracks the state of the whole
	// application.

	public String[] targets;
	public ImageLoader[] targetSources;

	public boolean crossCompareSelf = false;
	public boolean crossCompareOthers = false;

	public IHashAlgorithm algorithm = new DifferenceHash();
	public boolean touchDisk = true;
	public boolean verbose = false;

	public Action exactMatchAction = null;
	public Action partialMatchAction = null;
	public UnmatchedAction nonMatchAction = null;

	public Options(String[] args) {
		ArgParser parser = new ArgParser(args);

		if (parser.switchPresent(new Switch("-h", "--help"))) {
			System.out.println(Main.HELP);
			System.exit(0);
		}

		if (parser.switchPresent(new Switch("-v", "--verbose"))) this.verbose = true;
		if (parser.switchPresent(new Switch("-t", "--test"))) this.touchDisk = false;

		if (parser.switchPresent(new Switch("-s", "--same"))) this.crossCompareSelf = true;
		if (parser.switchPresent(new Switch("-o", "--other"))) this.crossCompareOthers = true;

		if (!(this.crossCompareSelf || this.crossCompareOthers)) {
			System.out.println("Must either compare sources to themselves, or to others, or both.");
			System.exit(0);
		}

		// Parse Actions from args
		if (parser.switchPresent(new Switch("-e", "--exact"))) {
			switch (parser.lastSwitchValue("noop").toLowerCase()) {
			case "noop":
				exactMatchAction = Actions.noop;
				break;
			case "gui":
				exactMatchAction = Actions.gui;
				break;
			case "print":
				exactMatchAction = Actions.print;
				break;
			case "delete":
				exactMatchAction = Actions.delete;
				break;
			default:
				exactMatchAction = Actions.noop;
				break;
			}
		}
		if (parser.switchPresent(new Switch("-p", "--partial"))) {
			switch (parser.lastSwitchValue("noop").toLowerCase()) {
			case "noop":
				partialMatchAction = Actions.noop;
				break;
			case "gui":
				partialMatchAction = Actions.gui;
				break;
			case "print":
				partialMatchAction = Actions.print;
				break;
			case "delete":
				partialMatchAction = Actions.delete;
				break;
			default:
				partialMatchAction = Actions.noop;
				break;
			}
		}
		if (parser.switchPresent(new Switch("-n", "--nonmatched"))) {
			switch (parser.lastSwitchValue("noop").toLowerCase()) {
			case "noop":
				nonMatchAction = Actions.unnoop;
				break;
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
		int algSideLength = 16;
		if (parser.switchPresent(new Switch("-p", "--phash"))) {
			this.algorithm = new PerceptualHash(parseSideLength(parser, algSideLength), mode);
		} else if (parser.switchPresent(new Switch("-d", "--dhash"))) {
			this.algorithm = new DifferenceHash(parseSideLength(parser, algSideLength), mode);
		} else if (parser.switchPresent(new Switch("-a", "--ahash"))) {
			this.algorithm = new AverageHash(parseSideLength(parser, algSideLength), mode);
		} else {
			this.algorithm = new DifferenceHash();
		}

		this.targets = parser.targets();
		this.targetSources = makeSources(this.targets);
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

		// Find all contained folders
		List<List<File>> fullTraces = new ArrayList<>();
		for (File targetFolder : targetFiles) {
			fullTraces.add(allDirectories(targetFolder));
		}

		// Make sure none of the folders are inside of each other.
		for (int i = 0; i < targets.length; i++) {
			List<File> containedFolders1 = fullTraces.get(i);
			for (int j = i + 1; j < targets.length; j++) {
				if (!Collections.disjoint(containedFolders1, fullTraces.get(j))) {
					System.out.println("One of the targets contains another.");
					System.out.println(
							fullTraces.get(i).get(0) + " and " + fullTraces.get(j).get(0) + " are not disjoint.");
				}
			}
		}

		// Everything is now validated. I am finally content.

		return loaders;
	}

	private static List<File> allDirectories(File target) {
		List<File> alldirs = new ArrayList<>();
		alldirs.add(target);

		List<File> toTraverse = new ArrayList<>();
		for (;;) {
			File file = toTraverse.remove(toTraverse.size() - 1);
			if (!file.isDirectory()) continue;
			else alldirs.add(file);

			File[] files = file.listFiles();
			for (File f : files) {
				List<File> containedFolders = Arrays.asList(f.listFiles()).stream().filter(fi -> fi.isDirectory())
						.filter(fi -> !Files.isSymbolicLink(fi.toPath())).collect(Collectors.toList());
				toTraverse.addAll(containedFolders);
				alldirs.addAll(containedFolders);
			}

			if (toTraverse.isEmpty()) break;
		}
		return alldirs;
	}
}
