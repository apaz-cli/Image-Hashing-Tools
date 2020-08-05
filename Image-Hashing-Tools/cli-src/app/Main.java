package app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import app.commands.Command;
import app.commands.GuidedMerge;
import app.commands.ListDuplicates;
import app.commands.PartialMerge;
import app.commands.TrimExact;

public class Main {

	// @nof
	private static String HELP = 
			"Usage: ihtools [options]... command [arguments]...\n" + 
			"Options:\n" + 
			"  -h, --help          Display this message and exit.\n" +
			"  -c, --commands      Display a help message detailing the usage of each command.\n" +
			"  -v, --verbose       Print all the files that are created, move or are deleted as a result of this program.\n" +
			"  -t, --test          Print all the files that would be created, moved, or be deleted as a result of this program, but don't.\n" + 
			"  -r, --regex         Use regex pattern matching for file and Sources instead of glob syntax.\n" +
			"  -p, --pHash         Use the pHash algorithm for comparison.\n" +
			"  -d, --dHash         Use the dHash algorithm for comparison.\n"; 
		  
	
	private static String COMMANDHELP = 
			"╔═══════════════╤══════════════════════╤═══════════════════════════════════════╗\n" + 
			"║Command        │ Arguments            │ Description                           ║\n" +
			"╟───────────────┼──────────────────────┼───────────────────────────────────────╢\n" +  
			"║partialmerge   │ fromSource toSource  │ Delete images that are identical to   ║\n" + 
			"║               │                      │ one in toSource, leave images with    ║\n" +
			"║               │                      │ partial matches, and move the rest,   ║\n" + 
			"║               │                      │ avoiding name collisions.             ║\n" +
			"╟───────────────┼──────────────────────┼───────────────────────────────────────╢\n" + 
			"║guidedmerge    │ fromSource toSource  │ Do a partialmerge, but a gui pops up  ║\n" + 
			"║               │                      │ for partial matches asking what to do.║\n" +
			"╟───────────────┼──────────────────────┼───────────────────────────────────────╢\n" + 
			"║trimexact      │ fromSource refSource │ Delete images in fromSource with an   ║\n" + 
			"║               │                      │ identical duplicate in refSource. Note║\n" + 
			"║               │                      │ that fromSource and refSource can be  ║\n" + 
			"║               │                      │ the same Source.                      ║\n" +
			"╟───────────────┼──────────────────────┼───────────────────────────────────────╢\n" +
			"║listduplicates │ fromSource refSource │ Cross-compare and list the partial and║\n" + 
			"║               │                      │ exact duplicates of images in from    ║\n" + 
			"║               │                      │ Source to the ones in refSource. Like ║\n" +
			"║               │                      │ with trimexact, these may be the same.║\n" +
			"╚═══════════════╧══════════════════════╧═══════════════════════════════════════╝\n" + 
			" Note:                                                                          \n" + 
			"        Arguments for Sources can be either a folder containing images, or      \n" + 
			"    alternatively a text file containing links to images, one on each line. The \n" + 
			"    images will be loaded or downloaded, hashed, and matches will be reloaded   \n" + 
			"    or redownloaded to check for exact duplicates. Sources will be edited at    \n" + 
			"    the end of the process to reflect changes.                                  \n";
	// @dof

	private static HashMap<String, app.commands.Command> commands = new HashMap<>();
	static {
		commands.put("partialmerge", new PartialMerge());
		commands.put("trimexact", new TrimExact());
		commands.put("guidedmerge", new GuidedMerge());
		commands.put("listduplicates", new ListDuplicates());
	}

	public static void main(String[] args) {
		// Print usage if no args
		if (args.length == 0) {
			System.out.println(HELP);
			System.exit(0);
		}

		// Exit if help
		for (String arg : args) {
			if (arg.equals("-h") || arg.equals("--help")) {
				System.out.println(HELP);
				System.exit(0);
			} else if (arg.equals("-c") || arg.equals("--commands")) {
				System.out.println(COMMANDHELP);
				System.exit(0);
			}
		}

		// The options constructor strips the flags from the arguments, as well as the
		// command name. It contains all the configurations necessary for the command to
		// run, as well as which command to run.
		ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
		Options options = new Options(arguments);

		Command command = parseCommand(arguments);
		command.acceptOptions(options);
		arguments.trimToSize();
		command.acceptArgs(arguments);

		command.run();
	}

	// The flags have been removed from the arg list.
	private static app.commands.Command parseCommand(List<String> args) {
		Command command = commands.get(args.remove(0).toLowerCase());
		if (command == null) {
			System.out.println("Please enter a valid command. Use -c or --commands to display a list.");
			System.exit(0);
		}
		return command;
	}

}
