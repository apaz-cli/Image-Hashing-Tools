package app.argparse;

import java.util.HashMap;
import java.util.TreeSet;

// Adapted (with substantial modification) from http://tutorials.jenkov.com/java-howto/java-command-line-argument-parser.html
public class ArgParser {

	private String[] args = null;

	private HashMap<String, Integer> switchIndexes = new HashMap<String, Integer>();
	private TreeSet<Integer> takenIndexes = new TreeSet<Integer>(); // Switches are always already taken

	private Switch last = null;

	// Assumes switch is present, will explode if not. We always call
	// switchPresent() before this.
	private int getIndex(Switch sw) {
		Integer i1 = switchIndexes.get(sw.getShort());
		Integer i2 = switchIndexes.get(sw.getLong());
		return i1 != null ? i1 : i2;
	}

	public ArgParser(String[] args) {
		this.args = args;
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				switchIndexes.put(args[i].toLowerCase(), i);
				takenIndexes.add(i);
			}
		}
	}

	/***********/
	/* Present */
	/***********/

	public boolean switchPresent(Switch sw) {
		this.last = sw;
		return switchIndexes.containsKey(sw.getShort()) || switchIndexes.containsKey(sw.getLong());
	}

	public boolean anySwitchesPresent(Switch... switches) {
		for (Switch name : switches) {
			if (switchPresent(name)) return true;
		}
		return false;
	}

	/*********/
	/* Value */
	/*********/
	
	// (Returns a string value for what comes after the switch)

	public String lastSwitchValue() {
		return lastSwitchValue(null);
	}

	public String lastSwitchValue(String defaultValue) { return last != null ? switchValue(last, defaultValue) : null; }

	public String switchValue(Switch sw) { return switchValue(sw, null); }

	public String switchValue(Switch sw, String defaultValue) {
		if (!switchPresent(sw)) return defaultValue;

		int switchIndex = this.getIndex(sw);
		if (switchIndex + 1 < args.length) {
			takenIndexes.add(switchIndex + 1);
			return args[switchIndex + 1];
		}
		return defaultValue;
	}

	public Long switchLongValue(Switch sw) throws NumberFormatException { return switchLongValue(sw, null); }

	public Long switchLongValue(Switch sw, Long defaultValue) throws NumberFormatException {
		String switchValue = switchValue(sw, null);

		if (switchValue == null) return defaultValue;
		return Long.parseLong(switchValue);
	}

	public Double switchDoubleValue(Switch sw) throws NumberFormatException { return switchDoubleValue(sw, null); }

	public Double switchDoubleValue(Switch sw, Double defaultValue) throws NumberFormatException {
		String switchValue = switchValue(sw, null);

		if (switchValue == null) return defaultValue;
		return Double.parseDouble(switchValue);
	}

	public String[] switchValues(Switch sw) {
		if (!switchPresent(sw)) return new String[0];

		int switchIndex = getIndex(sw);

		int nextArgIndex = switchIndex + 1;
		while (nextArgIndex < args.length && !args[nextArgIndex].startsWith("-")) {
			takenIndexes.add(nextArgIndex);
			nextArgIndex++;
		}

		String[] values = new String[nextArgIndex - switchIndex - 1];
		for (int j = 0; j < values.length; j++) {
			values[j] = args[switchIndex + j + 1];
		}
		return values;
	}

	/***********/
	/* Targets */
	/***********/

	// For use after every single switch is processed.
	public String[] targets() {
		String[] targetArray = new String[args.length - takenIndexes.size()];
		int targetIndex = 0;
		for (int i = 0; i < args.length; i++) {
			if (!takenIndexes.contains(i)) {
				targetArray[targetIndex++] = args[i];
			}
		}

		return targetArray;
	}

}
