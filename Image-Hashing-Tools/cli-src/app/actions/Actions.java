package app.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import app.argparse.Options;
import app.graph.VertexCover;
import app.gui.MergeWindow;
import app.util.TrackedMatch;
import hash.ImageHash;
import pipeline.dedup.HashMatch;
import utils.ImageUtils;
import utils.Pair;

public abstract class Actions {
	public static final Action noop = (matches, options) -> {};
	public static final Action print = (matches, options) -> matches.stream().forEach(h -> printMatchSources(h));
	public static final Action deletebutone = (matches, options) -> trashImages(allButOneFromEachMatchGroup(matches),
			options);
	public static final Action deletecover = (matches, options) -> {
		// Collect the matches into a list and trim the duplicates.
		List<ImageHash> hashSourcePairs = new ArrayList<>();
		for (TrackedMatch m : matches) {
			ImageHash p1 = m.getFirst();
			ImageHash p2 = m.getSecond();
			if (!hashSourcePairs.contains(p1)) hashSourcePairs.add(p1);
			if (!hashSourcePairs.contains(p2)) hashSourcePairs.add(p2);
		}

		// Convert the matches to the same format
		List<Pair<ImageHash, ImageHash>> edges = matches.stream().map(tm -> new Pair<>(tm.getFirst(), tm.getSecond()))
				.collect(Collectors.toList());

		// Take the min vertex cover of that list of edges
		List<ImageHash> toDelete = VertexCover.approxMinVertexCover(hashSourcePairs, edges);

		// Delete, prompting the user first if requested.
		trashImages(toDelete, options);
	};
	public static final Action gui = (matches,
			options) -> { MergeWindow win = new MergeWindow(matches, options); win.resolve(); win.destroy(); };

	public static final UnmatchedAction unnoop = (unmatched, options) -> {};
	public static final UnmatchedAction unprint = (unmatched, options) -> {
		System.out.println("Unmatched images:");
		unmatched.stream().map(hash -> hash.getSource()).forEach(System.out::println);
	};
	public static final UnmatchedAction unmove = (unmatched, options) -> {
		if (!options.crossCompare) {
			if (options.verbose) System.out.println("Nothing to move, only one image source. Unmatched move skipped.");
			return;
		}

		final File destFolder = options.targetSources[1].getOriginalFolder();

		// List of sources and destinations
		List<Pair<File, File>> toMove = unmatched.parallelStream()
				.map(h -> new Pair<>(new File(h.getSource()), new File(destFolder, new File(h.getSource()).getName())))
				.collect(Collectors.toList());

		// Move them
		for (Pair<File, File> m : toMove) {
			try {
				if (options.touchDisk) Files.move(m.getKey().toPath(),
						ImageUtils.avoidNameCollision(m.getValue()).toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	public static final UnmatchedAction undelete = (unmatched, options) -> { trashImages(unmatched, options); };

	/******************/
	/* Helper Methods */
	/******************/

	private static List<ImageHash> allButOneFromEachMatchGroup(List<TrackedMatch> matches) {
		List<List<ImageHash>> matchLists = new ArrayList<>();
		for (TrackedMatch match : matches) {

			// Check to see if one of the lists contains one of the hashes in the match.
			// If so, then add all the items from the match to the list.
			// If not, make a new list.
			boolean listFound = false;
			for (List<ImageHash> matchList : matchLists) {
				boolean containsFirst = matchList.contains(match.getFirst());
				boolean containsSecond = matchList.contains(match.getSecond());

				if (containsFirst) if (!containsSecond) {
					matchList.add(match.getSecond());
					listFound = true;
					break;
				}
				if (containsSecond) if (!containsFirst) {
					matchList.add(match.getFirst());
					listFound = true;
					break;
				}
			}
			if (!listFound)
				matchLists.add(new ArrayList<>(Arrays.asList(new ImageHash[] { match.getFirst(), match.getSecond() })));
		}

		return matchLists.stream().flatMap(list -> list.subList(1, list.size()).stream()).collect(Collectors.toList());
	}

	public static void trashImages(List<ImageHash> hashes, Options options) {
		Set<String> filenames = hashes.stream().map(p -> p.getSource()).collect(Collectors.toSet());
		if (options.verbose) {
			System.out.println(options.touchDisk ? "This action will trash the following files:"
					: "This action would (but will not, because of the -t flag) delete:");
			for (String fn : filenames) System.out.println(fn);
			if (options.touchDisk) {
				System.out.println(
						"If you do not want this, terminate the program now with ctrl + c. To delete the files and continue, press Enter.");
				Scanner pause = new Scanner(System.in);
				pause.nextLine();
				pause.close();
			}
		}

		// Trash the files
		filenames.stream().map(fn -> new File(fn)).forEach(f -> {
			if (options.verbose) System.out.println("Deleting: " + f);
			if (options.touchDisk) try {
				// Handles everything, from name collisions to creating the trash folder.
				ImageUtils.moveToTrash(f);
			} catch (IOException e) {
			}
		});
	}

	private static void printMatchSources(HashMatch match) {
		System.out.println(new StringBuilder("<").append(match.getFirst().getSource()).append("|")
				.append(match.getSecond().getSource()).append(">"));
	}
}
