package app.commands;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import hash.ImageHash;
import pipeline.ImageSource;
import pipeline.dedup.HashMatch;
import pipeline.sources.ImageLoader;

class GuidedMerge extends Command {

	@Override
	public void runCommand() throws IOException {

		PartialMerge subCommand = new PartialMerge();
		subCommand.options = this.options;
		subCommand.runCommand();
		this.same = subCommand.same;

		List<HashMatch> partialMatches = subCommand.hashResults.getPossibleMatches();
		ImageSource[] sources = subCommand.sources;

		// Free the all the memory after having fetched everything
		subCommand = null;

		try {
			class WindowBox {
				public MergeWindow window = null;
			}
			final WindowBox box = new WindowBox();
			SwingUtilities.invokeAndWait(() -> { box.window = new MergeWindow(this, partialMatches, sources); });

			Set<ImageHash> toDelete = box.window.getToDelete();
			moveDelete(partialMatches, toDelete, sources);

		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

	private void moveDelete(List<HashMatch> partialMatches, Set<ImageHash> toDelete, ImageSource[] sources) {
		if (same) { // delete
			if (sources[0] instanceof ImageLoader) {
				for (HashMatch m : partialMatches) {
					ImageHash first = m.getFirst(), second = m.getSecond();
					if (toDelete.contains(first)) {
						
					}
					if (toDelete.contains(second)) {

					}
				}
			}
		} else { // move

		}
	}

}
