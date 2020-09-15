package app.commands;

import java.util.Arrays;
import java.util.HashMap;

import app.argparse.Options;

public class Main {

	// @nof
	public static String HELP = 
			"Usage: ihtools [options]... command [arguments]...\n" + 
			"Options:\n" + 
			"  -h, --help          Display this message and exit.\n" +
			"  -c, --commands      Display a help message detailing the usage of each command.\n" +
			"  -v, --verbose       Print all the files that are created, move or are deleted as a result of this program.\n" +
			"  -t, --test          Print all the files that would be created, moved, or be deleted as a result of this program, but don't.\n" + 
			"  -r, --regex         Use regex pattern matching for file and Sources instead of glob syntax.\n" +
			"  -p, --pHash         Use the pHash algorithm for comparison.\n" +
			"  -d, --dHash         Use the dHash algorithm for comparison.\n"; 
		  
	
	public static String COMMANDHELP = 
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
			"    A 'source' can be either a folder containing images, or a plain text file   \n" +
			" containing links to images, one on each line. The images will be loaded or     \n" + 
			" downloaded, then hashed, to check for duplicates, then the appropriate action  \n" + 
			" will be taken, depending on the command.                                       \n" + 
			"                                                                                \n" + 
			"    Another thing of relevance to note is that arguments marked toSource or     \n" + 
			" refSource are not actually themselves cross-compared by these commands.        \n";
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

		Command command = commands.get(args[0]);
		if (command == null) {
			System.out.println("Please enter a valid command. Use -c or --commands to display a list.");
			System.exit(0);
		}

		Options options = new Options(Arrays.copyOfRange(args, 1, args.length));
		command.acceptOptions(options);

		command.run();
		
		return;
	}

}
