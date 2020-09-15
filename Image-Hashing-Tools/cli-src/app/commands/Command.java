package app.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import app.argparse.Options;
import hash.ImageHash;
import image.IImage;
import image.implementations.RGBAImage;
import pipeline.ImageSource;
import pipeline.dedup.HashMatch;
import pipeline.sources.ImageLoader;
import pipeline.sources.URLDownloader;
import utils.Triple;

abstract class Command {

	protected app.argparse.Options options = null;

	// All the commands are constructed statically, so this sort of takes the place
	// of the constructor and provides it what it needs to know at runtime.
	public void acceptOptions(Options options) { this.options = options; }

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

	// Returns null if could not discern type of source.
	private ImageSource parseSource(String argument) {
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
							final URLDownloader downloader = new URLDownloader(f);
							Runtime.getRuntime().addShutdownHook(new Thread(() -> {
								try {
									if (options.verbose) System.out.println("Saving changes to URL files.");
									if (options.touchDisk) downloader.close();
								} catch (IOException e) {
									System.err.println("There was an error saving the downloader file.");
									e.printStackTrace();
								}
							}));
							return downloader;
						} catch (IllegalArgumentException e) {}
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

	protected ImageSource[] makeSources(int minSources, int maxSources) {
		ImageSource[] sources = new ImageSource[maxSources];
		if (this.options.targets.length < minSources || options.targets.length > maxSources) {
			System.out.println("Not enough or too many targets provided. Expected " + minSources + "-" + maxSources
					+ " image sources. Got: " + Arrays.deepToString(options.targets));
			System.exit(0);
			return null;
		}

		int sourceNum = 0;
		for (; sourceNum < minSources && sourceNum < options.targets.length; sourceNum++) {
			sources[sourceNum] = this.parseSource(options.targets[sourceNum]);
		}

		return Arrays.copyOf(sources, sourceNum);
	}

	protected static boolean sameSource(String[] targets) {
		boolean sameSource;
		try {
			sameSource = targets[0].equals(targets[1]);
			sameSource = sameSource || Files.isSameFile(new File(targets[0]).toPath(), new File(targets[1]).toPath());
		} catch (IOException e) {
			sameSource = false;
		}
		return sameSource;
	}

	protected void exitMessage(String message) {
		System.out.println(message);
		System.exit(0);
	}

	protected void errorMessage(String message) {
		System.err.println(message);
		System.exit(2);
	}

	protected List<File> findFilesFromPattern(String pattern) throws IOException {
		PathMatcher pathMatcher = FileSystems.getDefault()
				.getPathMatcher((options.globMatch ? "glob:" : "regex:") + pattern);

		return Files.walk(new File(System.getProperty("user.dir")).toPath()).filter(p -> pathMatcher.matches(p))
				.map(p -> p.toFile()).collect(Collectors.toList());
	}

	/**************/
	/* File Moves */
	/**************/

	protected void saveImage(IImage<?> img, File f, String format) throws IOException {
		if (options.verbose) System.out.println((f.exists() ? "Saving image: " : "Overwriting image: ") + f);
		if (options.touchDisk) ImageIO.write(img.toBufferedImage(), format, f);
	}

	protected void deleteImage(File f) {
		if (options.verbose) System.out.println("Deleting image: " + f);
		if (options.touchDisk) f.delete();
	}

	protected void moveImage(File from, File to) throws IOException {
		if (!from.exists()) {
			if (options.verbose) System.out.println("The file " + from + " was moved or deleted already.");
			return;
		}
		if (options.verbose)
			System.out.println("Moving image from " + from + " to " + to + (to.exists() ? " (Overwriting)" : ""));
		if (options.touchDisk) Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	/*************/
	/* URL Moves */
	/*************/

	protected void saveURL(String url, URLDownloader to) {
		if (options.verbose) System.out.println("Preparing to save url: " + url);
		if (options.touchDisk) to.addToSource(url);
	}

	protected void deleteURL(String url, URLDownloader from) {
		if (options.verbose) System.out.println("Preparing to remove url: " + url);
		if (options.touchDisk) from.removeFromSource(url);
	}

	protected void moveURL(String url, URLDownloader from, URLDownloader to) {
		if (options.verbose) System.out.println("Preparing to move url: " + url);
		if (options.touchDisk) {
			to.addToSource(url);
			from.removeFromSource(url);
		}

	}

	/**************/
	/* List Moves */
	/**************/

					// If same, deletes the source with the longer name. If not same, deletes the
					// second one.
	protected void deleteExact(List<ImageHash> exactDuplicates, ImageSource from) {
		if (from instanceof ImageLoader) {
			if (same) {
				for (ImageHash hash : exactDuplicates) {
					this.deleteImage(new File(hash.getSource()));
				}
			} else {
				for (ImageHash hash : exactDuplicates) {
					this.deleteImage(new File(hash.getSource()));
				}
			}
		} else if (from instanceof URLDownloader) {
			URLDownloader fromSource = (URLDownloader) from;
			if (same) {
				for (ImageHash hash : exactDuplicates) {
					this.deleteURL(hash.getSource(), fromSource);
				}
			} else {
				for (ImageHash hash : exactDuplicates) {
					this.deleteURL(hash.getSource(), fromSource);
				}
			}
		} else {
			throw new IllegalStateException("Moving not implemented for image source: " + from.getClass().getName());
		}
	}

	// Moves images from the first source to the second
	protected void moveUnmatched(List<ImageHash> unmatched, ImageSource from, ImageSource to) {
		// Nothing to move
		if (same) return;

		if (from instanceof ImageLoader) {
			for (ImageHash h : unmatched) {
				String source = h.getSource();
				File fromFile = new File(source);
				File moveTo = new File(((ImageLoader) to).getFolder(), fromFile.getName());

				try {
					this.moveImage(fromFile, moveTo);
				} catch (IOException e) {
					System.err.println("Failed to move file: " + fromFile + " to " + moveTo);
				}
			}
		} else if (from instanceof URLDownloader) {
			for (ImageHash h : unmatched) {
				this.moveURL(h.getSource(), (URLDownloader) from, (URLDownloader) to);
			}
		} else {
			throw new IllegalStateException("Moving not implemented for image source: " + from.getClass().getName());
		}
	}

	/************/
	/* Compares */
	/************/

	protected HashResults compare(ImageSource first, ImageSource second) {
		return this.same ? crossCompareSame(first) : crossCompareDifferent(first, second);
	}

	protected class HashResults extends Triple<List<HashMatch>, List<HashMatch>, List<ImageHash>> {
		public HashResults(List<HashMatch> first, List<HashMatch> second, List<ImageHash> third) {
			super(first, second, third);
		}

		List<HashMatch> getExactMatches() { return this.getFirst(); }

		List<HashMatch> getPossibleMatches() { return this.getSecond(); }

		List<ImageHash> getNonMatched() { return this.getThird(); }

	}

	protected boolean same;

	protected HashResults crossCompareDifferent(ImageSource firstSource, ImageSource secondSource) {

		final List<ImageHash> firstSourceCollection = firstSource.parallelStreamHashes(options.algorithm)
				.collect(Collectors.toList());

		// Full cross compare
		List<HashMatch> exact = new Vector<>();
		List<HashMatch> possible = new Vector<>();
		List<ImageHash> secondSourceNonMatched = secondSource.parallelStreamHashes(options.algorithm)
				.flatMap(second -> {
					return firstSourceCollection.stream().map(first -> {
						if (second.matches(first)) {
							HashMatch foundMatch = new HashMatch(first, second);

							RGBAImage img1 = null, img2 = null;
							try {
								img1 = first.loadFromSource().toRGBA();
								img2 = second.loadFromSource().toRGBA();
							} catch (IOException e) {
								System.err.println("Was unable to read from the image source: " + foundMatch);
								possible.add(foundMatch);
							}

							if (img1.equals(img2)) {
								exact.add(foundMatch);
							} else {
								possible.add(foundMatch);
							}

							return null;
						} else {
							return first;
						}

					});
				}).filter(Objects::nonNull).collect(Collectors.toList());

		return new HashResults(exact, possible, secondSourceNonMatched);
	}

	protected HashResults crossCompareSame(ImageSource source) {
		List<HashMatch> exact = new ArrayList<>();
		List<HashMatch> possible = new ArrayList<>();
		List<ImageHash> nonMatched;

		// This is necessary because generic arrays are not allowed.
		class MarkedHash extends ImageHash {

			private static final long serialVersionUID = 1L;

			MarkedHash(ImageHash h) { super(h); }

			public boolean matched = false;
		}

		// To array
		MarkedHash[] hashes;
		{
			List<MarkedHash> allHashes = source.parallelStreamHashes(options.algorithm)
					.map(hash -> new MarkedHash(hash)).collect(Collectors.toList());
			hashes = allHashes.toArray(new MarkedHash[allHashes.size()]);
		}

		// Triangular cross compare
		for (int i = 0; i < hashes.length; i++) {
			MarkedHash h1 = hashes[i];
			for (int j = i + 1; j < hashes.length; j++) {
				MarkedHash h2 = hashes[j];

				boolean matches = options.algorithm.matches(h1, h2);
				if (matches) {
					// Mark that the images have found matches so that they're excluded from
					// nonMatched, and then add the match to exact or possible after loading the
					// images and checking where it should go.

					h1.matched = true;
					h2.matched = true;

					// No MarkedHashes escape this method.
					HashMatch match = new HashMatch(new ImageHash(h1), new ImageHash(h2));
					try {
						if (matchIsExact(match)) {
							exact.add(match);
						} else {
							possible.add(match);
						}
					} catch (IOException e) {
						possible.add(match);
					}
				}

				// Else do nothing, the images that don't get matched just don't get marked, and
				// are cleaned up below.

			}
		}

		nonMatched = Arrays.asList(hashes).stream().filter(h -> !h.matched).map(toCopy -> new ImageHash(toCopy))
				.collect(Collectors.toList());

		return new HashResults(exact, possible, nonMatched);

	}

	private static boolean matchIsExact(HashMatch match) throws IOException {
		RGBAImage img1 = null;
		try {
			img1 = match.loadFirst().toRGBA();
		} catch (IOException e) {
			System.err.println("Was unable to read from the image source: " + match.getFirst().getSource());
			return false;
		}

		RGBAImage img2 = null;
		try {
			img2 = match.loadSecond().toRGBA();
		} catch (IOException e) {
			System.err.println("Was unable to read from the image source: " + match.getSecond().getSource());
			return false;
		}

		return img1.equals(img2);
	}

}
