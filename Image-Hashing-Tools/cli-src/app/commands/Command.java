package app.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hashstore.LinearHashStore;
import image.IImage;
import image.implementations.SourcedImage;
import pipeline.ImageSource;
import pipeline.dedup.HashMatch;
import pipeline.hasher.ImageHasher;
import pipeline.sources.SavingImageSource;
import pipeline.sources.downloader.URLDownloader;
import pipeline.sources.loader.ImageLoader;
import utils.Pair;

public abstract class Command implements Runnable {

	protected app.Options commandOptions = null;
	protected List<String> args = new ArrayList<>();

	@Override
	public void run() {
		try {
			this.runCommand();
		} catch (IOException e) {
			System.out.println("There was an error running the command. Please report the following on Github.");
			e.printStackTrace();
			System.exit(2);
		}
	};

	abstract public void runCommand() throws IOException;

	public void acceptOptions(app.Options options) {
		this.commandOptions = options;
	}

	// Returns null if could not discern type of source.
	protected SavingImageSource parseSource(String argument) {
		argument = argument.trim();
		try {
			File f = new File(argument);
			if (f.exists()) {
				if (f.isDirectory()) {
					return new ImageLoader(f);
				} else {
					String mime = Files.probeContentType(f.toPath());
					if (mime.equals("text/plain")) {
						try {
							return new URLDownloader(f);
						} catch (IllegalArgumentException e) {
						}
					} else if (mime.contains("image")) {
						exitMessage("This is an image file, not a folder containing the image.");

					}
					exitMessage(
							"The image source is a file that exists and is not a directory, but is not a plain text file. Could not create a source.");

					return null;
				}
			} else {
				exitMessage("The file specified does not exist.");
			}

		} catch (IOException e) {
			exitMessage("Couldn't parse an image source from " + argument
					+ ". Please see --help for more details on what this argument should be.");
		}
		return null;
	}

	protected void exitMessage(String message) {
		System.out.println(message);
		System.exit(0);
	}
	
	protected void errorMessage(String message) {
		System.err.println(message);
		System.exit(2);
	}

	public void acceptArgs(List<String> arguments) {
		this.args = arguments;
	}

	protected List<File> findFilesFromPattern(String pattern) throws IOException {
		PathMatcher pathMatcher = FileSystems.getDefault()
				.getPathMatcher((commandOptions.globMatch ? "glob:" : "regex:") + pattern);

		return Files.walk(new File(System.getProperty("user.dir")).toPath()).filter(p -> pathMatcher.matches(p))
				.map(p -> p.toFile()).collect(Collectors.toList());
	}

	protected void saveImage(IImage<?> img, File f, String format) throws IOException {
		if (commandOptions.verbose) System.out.println((f.exists() ? "Saving image: " : "Overwriting image: ") + f);
		if (commandOptions.touchDisk) ImageIO.write(img.toBufferedImage(), format, f);
	}

	protected void deleteImage(HashMatch match, SavingImageSource fromSrc) {
		String s1 = match.getFirst().getSource();
		String s2 = match.getSecond().getSource();
		String toDelete = s1.length() > s2.length() ? s1 : s2;
		if (commandOptions.verbose) System.out.println("Deleting image: " + toDelete);
		if (commandOptions.touchDisk) fromSrc.removeFromSource(toDelete);
	}

	protected void deleteImage(ImageHash h, SavingImageSource fromSrc) {
		String src = h.getSource();
		if (commandOptions.verbose) System.out.println("Deleting image: " + src);
		if (commandOptions.touchDisk) fromSrc.removeFromSource(src);
	}

	protected void moveImage(File from, File to) throws IOException {
		if (commandOptions.verbose)
			System.out.println("Moving image from " + from + " to " + to + (to.exists() ? " (Overwriting)" : ""));
		if (commandOptions.touchDisk) Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	// These two do not close the sources.
	// Produces matches with images from <fromSource, refSource>.
	public static Pair<List<HashMatch>, List<HashMatch>> compareDifferent(IHashAlgorithm alg,
			final ImageSource fromSource, final ImageSource refSource) {
		List<ImageHash> refHashes = refSource.toIImageList().parallelStream().map(img -> alg.hash(img))
				.collect(Collectors.toList());

		// Find duplicates
		List<HashMatch> exact = new Vector<>();
		List<HashMatch> possible = new Vector<>();

		int threadNum = 8;
		ExecutorService es = Executors.newWorkStealingPool(threadNum);
		for (int i = 0; i < threadNum; i++) {
			Runnable r = () -> {
				IImage<?> img;
				while ((img = fromSource.next()) != null) {
					ImageHash fromHash = alg.hash(img);
					for (ImageHash refHash : refHashes) {
						if (refHash.matches(fromHash)) {
							try {
								HashMatch foundMatch = new HashMatch(fromHash, refHash);
								if (refHash.loadFromSource().toRGBA().equals(img.toRGBA())) {
									exact.add(foundMatch);
								} else {
									possible.add(foundMatch);
								}
							} catch (IOException e) {
								System.err.println("Was unable to read from the image source: " + refHash.getSource());
							}
						}
					}
				}
			};
			es.execute(r);
		}

		try {
			es.shutdown();
			es.awaitTermination(2L, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return new Pair<>(exact, possible);
	}

	// Produces matches that come from the same, single image source.
	public static Pair<List<HashMatch>, List<HashMatch>> compareSame(IHashAlgorithm alg, ImageSource s) {
		List<HashMatch> exact = new ArrayList<>();
		List<HashMatch> possible = new ArrayList<>();

		File temp = new File("temporary list of image hashes.txt");
		temp.delete();
		temp.deleteOnExit();
		LinearHashStore hs = null;
		try {
			hs = new LinearHashStore(temp);
		} catch (IOException e) {
			System.err.println("Was unable to write or create the file " + temp + ".");
			e.printStackTrace();
		}

		ImageHasher hasher = new ImageHasher(s, alg, hs);
		hasher.hashAll(10);

		List<HashMatch> matches = null;
		try {
			matches = hs.findMatches(500);
		} catch (IOException e1) {
			System.err.println("Was unable to read from the file " + temp + ".");
			e1.printStackTrace();
		}

		File matchDir = new File("Possible Matches");
		matchDir.mkdir();

		for (HashMatch match : matches) {
			SourcedImage img1 = null;
			try {
				img1 = match.loadFirst();
			} catch (IOException e) {
				System.err.println("Was unable to read from the image source: " + match.getFirst().getSource());
				continue;
			}
			SourcedImage img2 = null;
			try {
				img2 = match.loadSecond();
			} catch (IOException e) {
				System.err.println("Was unable to read from the image source: " + match.getSecond().getSource());
				continue;
			}

			if (img1.toRGBA().equals(img2.toRGBA())) {
				exact.add(match);
				continue;
			} else {
				possible.add(match);
			}
		}

		try {
			hs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Pair<>(exact, possible);
	}

}
