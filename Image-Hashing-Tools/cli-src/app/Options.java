package app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hash.IHashAlgorithm;
import hash.implementations.DifferenceHash;
import hash.implementations.PerceptualHash;

public class Options {

	public IHashAlgorithm algorithm = new DifferenceHash();
	public boolean touchDisk = true;
	public boolean verbose = false;
	public boolean globMatch = true;

	public Options(List<String> args) {
		List<String> flags = Arrays.asList(
				new String[] { "-v", "--verbose", "-t", "--test", "-r", "--regex", "-p", "--phash", "-d", "--dhash" });

		boolean algSeen = false;

		List<String> toRemove = new ArrayList<>();
		for (String arg : args) {
			if (flags.contains(arg.toLowerCase())) {
				// @nof
				if (!algSeen) {
					 if      (arg.contains("-p")) algorithm = new PerceptualHash();
					 else if (arg.contains("-d")) algorithm = new DifferenceHash();
				} 
				else if (arg.contains("-v")) verbose = true;
				else if (arg.contains("-t")) touchDisk = false;
				else if (arg.contains("-r")) globMatch = false;
				// @dof
				toRemove.add(arg);
			}
		}
		args.removeAll(toRemove);
	}
}
