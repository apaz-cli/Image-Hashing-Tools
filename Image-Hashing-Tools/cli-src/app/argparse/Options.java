package app.argparse;

import app.commands.Main;
import hash.IHashAlgorithm;
import hash.MatchMode;
import hash.implementations.AverageHash;
import hash.implementations.DifferenceHash;
import hash.implementations.PerceptualHash;

public class Options {

	// TARGETS
	public String[] targets;

	public IHashAlgorithm algorithm = new DifferenceHash();
	public boolean touchDisk = true;
	public boolean verbose = false;
	public boolean globMatch = true;

	public Options(String[] args) {
		ArgParser parser = new ArgParser(args);

		if (parser.switchPresent(new Switch("-h", "--help"))) {
			System.out.println(Main.HELP);
			System.exit(0);
		}
		if (parser.switchPresent(new Switch("-c", "--commands"))) {
			System.out.println(Main.COMMANDHELP);
			System.exit(0);
		}

		if (parser.switchPresent(new Switch("-v", "--verbose"))) this.verbose = true;
		if (parser.switchPresent(new Switch("-t", "--test"))) this.touchDisk = false;
		if (parser.switchPresent(new Switch("-r", "--regex"))) globMatch = false;

		MatchMode mode = MatchMode.NORMAL;
		if (parser.switchPresent(new Switch("-m", "--matchmode"))) {
			String mm = parser.lastSwitchValue("normal").toLowerCase();
			if (mm.equals("exact")) {
				mode = MatchMode.EXACT;
			} else if (mm.equals("strict")) {
				mode = MatchMode.STRICT;
			} else if (mm.equals("normal")) {
				// no-op
			} else if (mm.equals("sloppy")) {
				mode = MatchMode.SLOPPY;
			} else {
				System.out.println(
						"Could not parse the match mode. Expected nothing or either \"exact, strict, normal, or sloppy.\" Got: "
								+ mm);
				System.exit(0);
			}
		}

		int defaultSideLength = 16;
		if (parser.switchPresent(new Switch("-p", "--phash"))) {
			this.algorithm = new PerceptualHash(parseSideLength(parser, defaultSideLength), mode);
		} else if (parser.switchPresent(new Switch("-d", "--dhash"))) {
			this.algorithm = new DifferenceHash(parseSideLength(parser, defaultSideLength), mode);
		} else if (parser.switchPresent(new Switch("-a", "--ahash"))) {
			this.algorithm = new AverageHash(parseSideLength(parser, defaultSideLength), mode);
		}

		this.targets = parser.targets();
	}

	private Integer parseSideLength(ArgParser parser, int defaultSidelength) {
		String sideLength = parser.lastSwitchValue();
		if (sideLength == null) return defaultSidelength;
		try {
			return Integer.parseInt(sideLength);
		} catch (NumberFormatException e) {
			System.out.println(
					"Could not parse the side length for your image hashing algorithm. Expected nothing or an integer. Got: "
							+ sideLength);
			System.exit(0);
			return null;
		}
	}
}
