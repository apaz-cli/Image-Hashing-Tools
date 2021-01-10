package app.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.util.TrackedMatch;
import hash.ImageHash;
import pipeline.dedup.HashMatch;

/* 
 * Move: If an image from an ImageLoader matches an image from a different 
 * ImageLoader, the image from the preceding ImageLoader is moved into the
 * folder of the later ImageLoader.
 * 
 * Delete: Make a list of all the 
 */

public abstract class Actions {
	public static final Action noop = (matches, options) -> {};
	public static final Action print = (matches, options) -> matches.stream().forEach(h -> printMatch(h));
	public static final Action delete = (matches, options) -> {
		// TODO replace selection algorithm with minimum vertex cover
		List<List<ImageHash>> matchLists = new ArrayList<>();
		for (TrackedMatch match : matches) {

			// Check to see if one of the lists contains one of the hashes in the match.
			// If so, then add all the items from the match to the list.
			// If not, make a new list.
			for (List<ImageHash> matchList : matchLists) {
				boolean containsFirst = matchList.contains(match.getFirst());
				boolean containsSecond = matchList.contains(match.getSecond());

				boolean added = false;
				if (containsFirst) if (!containsSecond) {
					matchList.add(match.getSecond());
					added = true;
				}
				if (containsSecond) if (!containsFirst) {
					matchList.add(match.getFirst());
					added = true;
				}

				if (!added) {
					matchLists.add(
							new ArrayList<>(Arrays.asList(new ImageHash[] { match.getFirst(), match.getSecond() })));
				}
			}
		}

		// Now, delete all the files from each of the lists, except for the first item
		// of each list.
		for (List<ImageHash> matchList : matchLists) {
			for (int i = 1; i < matchList.size(); i++) {
				File toDelete = new File(matchList.get(i).getSource());
				if (options.verbose) System.out.println("Deleting File: " + toDelete);
				if (options.touchDisk) toDelete.delete();
			}
		}
	};
	public static final Action gui = (matches, options) -> {};

	public static final UnmatchedAction unnoop = (unmatched, options) -> {};
	public static final UnmatchedAction unprint = (unmatched, options) -> unmatched.stream()
			.map(hash -> hash.getSource()).forEach(System.out::println);
	public static final UnmatchedAction unmove = (unmatched, options) -> {

	};
	public static final UnmatchedAction undelete = (unmatched, options) -> {};

	/******************/
	/* Helper Methods */
	/******************/

	private static void printMatch(HashMatch match) {
		System.out.println(new StringBuilder("<").append(match.getFirst().getSource()).append("|")
				.append(match.getSecond().getSource()).append(">"));
	}
}
