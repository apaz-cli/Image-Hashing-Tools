package programs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.IntStream;

import hash.IHashAlgorithm;
import hash.ImageHash;
import hash.MatchMode;
import hash.implementations.AverageHash;
import hash.implementations.DifferenceHash;
import hash.implementations.PerceptualHash;
import image.implementations.RGBAImage;
import pipeline.sources.ImageLoader;

public class MFNDBenchmark {

	// Flip this on to hash all the images in the Mir-Flickr dataset.
	// Set to false if already hashed.
	public static boolean doHashes = false;

	/***********************/
	/* Algorithms to test: */
	/***********************/

	private static IHashAlgorithm phash64 = new PerceptualHash(8);
	private static IHashAlgorithm phash256 = new PerceptualHash(16);
	private static IHashAlgorithm phash1024 = new PerceptualHash(32);
	private static IHashAlgorithm phash4096 = new PerceptualHash(64);

	private static IHashAlgorithm dhash64 = new DifferenceHash(8);
	private static IHashAlgorithm dhash256 = new DifferenceHash(16);
	private static IHashAlgorithm dhash1024 = new DifferenceHash(32);
	private static IHashAlgorithm dhash4096 = new DifferenceHash(64);

	private static IHashAlgorithm ahash64 = new AverageHash(8);
	private static IHashAlgorithm ahash256 = new AverageHash(16);
	private static IHashAlgorithm ahash1024 = new AverageHash(32);
	private static IHashAlgorithm ahash4096 = new AverageHash(64);

	private static IHashAlgorithm[] allAlgs = new IHashAlgorithm[] { phash64, phash256, phash1024, phash4096, dhash64,
			dhash256, dhash1024, dhash4096, ahash64, ahash256, ahash1024, ahash4096 };

	/************************************************/
	/* Load the Mir-Flickr 1 million images dataset */
	/************************************************/

	/*
	 * Make sure you've converted all the images to png before this. ImageIO can't
	 * read some of the images from the Mir-Flickr dataset, so I ran them through
	 * python's PIL first and saved them as png. They're still pixel for pixel
	 * identical, just encoded slightly differently.
	 * 
	 * This took two days. PIL is slow.
	 */
	private static String datasetPath = "/media/apaz/c97ca85a-b079-4eae-b537-8e2dcb8da5f0/MFND/images";
	private static List<File> MirFlickrDataset;
	static {
		if (doHashes) {
			System.out.println("Loading the Mir-Flickr dataset.");
			MirFlickrDataset = new ImageLoader(datasetPath).getRemainingItems();
			System.out.println("Finished loading the Mir-Flickr dataset.");
			System.out.println("Found " + MirFlickrDataset.size() + " images.");
			System.out.println();

			if (MirFlickrDataset.size() == 0)
				throw new IllegalStateException();
		}
	}

	/**************************************/
	/* Make a folder to store the results */
	/**************************************/

	private static String resultDirPath = "/media/apaz/c97ca85a-b079-4eae-b537-8e2dcb8da5f0/MFND/results";
	private static File resultDir = new File(resultDirPath);
	static {
		if (!resultDir.exists())
			resultDir.mkdirs();
		if (!resultDir.exists())
			throw new IllegalStateException();
	}

	/*******************************/
	/* Run the actual calculations */
	/*******************************/
	public static void main(String[] args) throws IOException {

		// Inside the output folder, create files to store the hashes. Then make
		// writers.
		File[] hashFiles = new File[allAlgs.length];
		for (int i = 0; i < allAlgs.length; i++) {
			String fileName = "hashes_" + allAlgs[i].algName() + "" + allAlgs[i].getHashLength() + ".txt";
			hashFiles[i] = new File(resultDir, fileName);

			// Create the files and make really sure they were created.
			try {
				hashFiles[i].createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			if (!hashFiles[i].exists())
				throw new IllegalStateException();
		}

		// Hash all the images in the Mir-Flickr dataset, saving the hashes to files.
		if (doHashes) {
			// Create writers to save the hashes as soon as they are created.
			PrintWriter[] writers = new PrintWriter[allAlgs.length];
			for (int i = 0; i < allAlgs.length; i++)
				writers[i] = new PrintWriter(hashFiles[i]);

			System.out.println("Finished loading all the data. Now hashing images.");
			MirFlickrDataset.parallelStream().map(file -> {
				// Load the images into memory
				try {
					return new RGBAImage(file).addSource(file);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
					return null;
				}
			}).forEach(img -> {
				// Hash each image with all the algorithms and save them.
				for (int i = 0; i < allAlgs.length; i++) {
					ImageHash h = allAlgs[i].hash(img);
					writers[i].println(h);
					// writers[i].flush();
				}
			});

			// Close the writers now that we're done with them.
			for (PrintWriter w : writers)
				w.close();
		}

		// Now that all the images have been hashed and stored, load the lists of hashes
		// one by one and process them.
		System.out.println("Finished hashing all the images. Now comparing them.");
		List.of(hashFiles).stream().sequential().map(f -> {
			try {
				System.out.println("Loading: " + f);
				List<ImageHash> l = loadFromFile(f);
				System.out.println("Finished loading.");
				return l;
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
				return null;
			}
		}).forEach(hashList -> {
			try {
				if (hashList.size() == 0) {
					System.err.println("Please hash all the images first by changing the doHashes to true.");
					System.exit(1);
				}

				String currentAlgDescription = hashList.get(0).getAlgName()
						+ hashList.get(0).getAlgorithm().getHashLength();

				System.out.println("Comparing hashes.");
				ArrayList<HashPair>[] results = compareHashes(hashList);
				System.out.println("Finished comparing hashes. Moving on to comparing and saving.");
				compareToMFNDAndSave(results, currentAlgDescription);
				System.out.println("Finished comparing and saving " + currentAlgDescription + ".");

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		});

		// The output of this program is are the files in
		// results/<algname><hashlength>_<mode>_Type_1/2.txt
		return;
	}

	private static ArrayList<HashPair>[] compareHashes(List<ImageHash> currentHashList) {
		ImageHash[] arr = new ImageHash[currentHashList.size()];
		final ImageHash[] hashListArray = currentHashList.toArray(arr);

		// Cross compare every hash, storing the results of the comparisons here.
		List<HashPair> exactMatches = new Vector<>();
		List<HashPair> strictMatches = new Vector<>();
		List<HashPair> normalMatches = new Vector<>();
		List<HashPair> sloppyMatches = new Vector<>();

		int numThreads = 8;
		final List<ExecutorService> pools = IntStream.range(0, numThreads)
				.mapToObj(a -> Executors.newSingleThreadExecutor()).collect(Collectors.toList());

		for (int i1 = 0; i1 < hashListArray.length; i1++) {
			final int i = i1;
			pools.get(i % numThreads).execute(() -> {
				for (int j = i + 1; j < hashListArray.length; j++) {
					if (hashListArray[i].matches(hashListArray[j], MatchMode.EXACT))
						exactMatches
								.add(new HashPair(sourceToNumber(hashListArray[i]), sourceToNumber(hashListArray[j])));
					else if (hashListArray[i].matches(hashListArray[j], MatchMode.STRICT))
						strictMatches
								.add(new HashPair(sourceToNumber(hashListArray[i]), sourceToNumber(hashListArray[j])));
					else if (hashListArray[i].matches(hashListArray[j], MatchMode.NORMAL))
						normalMatches
								.add(new HashPair(sourceToNumber(hashListArray[i]), sourceToNumber(hashListArray[j])));
					else if (hashListArray[i].matches(hashListArray[j], MatchMode.SLOPPY))
						sloppyMatches
								.add(new HashPair(sourceToNumber(hashListArray[i]), sourceToNumber(hashListArray[j])));
				}
				System.out.println(i);
			});
		}

		strictMatches.addAll(exactMatches);
		normalMatches.addAll(strictMatches);
		sloppyMatches.addAll(normalMatches);

		pools.forEach(pool -> {
			try {
				pool.shutdown();
				pool.awaitTermination(5, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		});

		// Return the lists of matches for each mode.
		@SuppressWarnings("unchecked")
		ArrayList<HashPair>[] results = new ArrayList[] { new ArrayList<>(exactMatches), new ArrayList<>(strictMatches),
				new ArrayList<>(normalMatches), new ArrayList<>(sloppyMatches) };
		return results;
	}

	private static void compareToMFNDAndSave(ArrayList<HashPair>[] results, String algDescription) throws IOException {

		// Determine type 1 and type 2 error by comparing to the MFND dataset.
		List<List<HashPair>> type1Error = new Vector<>(); // False positives
		List<List<HashPair>> type2Error = new Vector<>(); // False negatives
		for (int i = 0; i < 4; i++) {
			type1Error.add(filterAll(new HashSet<>(sortPairs(results[i])), allPairs));
			type2Error.add(filterAll(new HashSet<>(allPairs), sortPairs(results[i])));
		}

		// Save the results
		String[] modes = new String[] { "Exact", "Strict", "Normal", "Sloppy" };
		IntStream.of(0, 1, 2, 3).parallel().forEach(i -> {
			// Set up file writers and name the output files
			String resultFileLabel = "results_" + algDescription + "_" + modes[i] + "_";
			File t1File = new File(resultDir, resultFileLabel + "Type1(FP).txt");
			File t2File = new File(resultDir, resultFileLabel + "Type2(FN).txt");
			PrintWriter t1Writer = null;
			PrintWriter t2Writer = null;
			try {
				t1Writer = new PrintWriter(t1File);
				t2Writer = new PrintWriter(t2File);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}

			// Write to the output files
			for (HashPair p : type1Error.get(i))
				t1Writer.println(p);
			for (HashPair p : type2Error.get(i))
				t2Writer.println(p);

			t1Writer.close();
			t2Writer.close();
		});
	}

	private static List<ImageHash> loadFromFile(File matchFile) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(matchFile)));
		List<ImageHash> textFile = reader.lines().map(line -> {
			try {
				return ImageHash.fromString(line);
			} catch (IllegalArgumentException | ClassNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
				return null;
			}
		}).collect(Collectors.toList());
		reader.close();

		return textFile;
	}

	/********************************/
	/* Helper Functions and Classes */
	/********************************/

	private static int sourceToNumber(ImageHash hash) {
		String path = hash.getSource().replace(".jpg", "");
		String[] spt = path.split(File.separator.equals("/") ? "/" : "\\\\");
		path = spt[spt.length - 1];
		spt = path.split("\\.");
		path = spt[0];
		return Integer.parseInt(path);
	}

	private static List<HashPair> filterAll(Set<HashPair> target, Collection<HashPair> by) {
		target.removeAll(by);
		return new ArrayList<>(target);
	}

	static class HashPair implements Comparable<HashPair> {
		int first;
		int second;

		HashPair(int first, int second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public int compareTo(HashPair other) {
			if (this.first < other.first)
				return -1;

			if (this.first > other.first)
				return 1;

			if (this.second < other.second)
				return -1;

			if (this.second > other.second)
				return 1;

			throw new IllegalStateException();
		}

		@Override
		public boolean equals(Object o) {
			HashPair other = (HashPair) o;
			return other.first == this.first && other.second == this.second;
		}

		@Override
		public int hashCode() {
			return this.first + this.second;
		}

		@Override
		public String toString() {
			return this.first + " " + this.second;
		}
	}

	/*******************/
	/* MFND DATALOADER */
	/*******************/

	// Pull the MFND dataset for the Mir-Flickr image dataset directly from the
	// download page.
	private static String indURL = "http://www.mir-flickr-near-duplicates.appspot.com/truthFiles/IND_clusters.txt";
	private static String nindURL = "http://www.mir-flickr-near-duplicates.appspot.com/truthFiles/NIND_clusters.txt";
	private static String duplicateURL = "http://www.mir-flickr-near-duplicates.appspot.com/truthFiles/duplicates.txt";
	private static List<HashPair> indPairs; // Images that are clearly the same (resizing, artifacts)
	private static List<HashPair> nindPairs; // Images that have a striking resemblance (But slightly different)
	private static List<HashPair> duplicatePairs; // Images that are pixel-identical
	private static List<HashPair> allPairs; // All of the above
	static {
		System.out.println("Downloading the MFND dataset.");
		indPairs = downloadUnclusterSort(indURL);
		nindPairs = downloadUnclusterSort(nindURL);
		duplicatePairs = downloadUnclusterSort(duplicateURL);
		allPairs = new ArrayList<HashPair>();
		allPairs.addAll(indPairs);
		allPairs.addAll(nindPairs);
		allPairs.addAll(duplicatePairs);
		allPairs = new ArrayList<>(new HashSet<>(allPairs));

		System.out.println("Finished downloading the MFND dataset.");
		System.out.println("Found:");
		System.out.println(indPairs.size() + " IND pairs, " + nindPairs.size() + " NIND pairs, " + duplicatePairs.size()
				+ " duplicate pairs. This makes " + allPairs.size() + " pairs total.");

		System.out.println();
	}

	private static List<HashPair> downloadUnclusterSort(String url) {
		try {
			return sortPairs(uncluster(parseClusters(downloadLines(url))));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	private static List<HashPair> sortPairs(ArrayList<HashPair> pl) {
		for (HashPair r : pl) {
			if (r.first == r.second)
				throw new IllegalStateException();
			if (r.first > r.second) {
				int tmp = r.first;
				r.first = r.second;
				r.second = tmp;
			}
		}

		Collections.sort(pl);
		return pl;
	}

	private static ArrayList<HashPair> uncluster(Stream<int[]> clusters) {
		ArrayList<HashPair> result = new ArrayList<>();
		clusters.forEach(c -> {
			for (int i = 0; i < c.length; i++)
				for (int j = i + 1; j < c.length; j++)
					result.add(new HashPair(c[i], c[j]));
		});
		return result;
	}

	private static Stream<int[]> parseClusters(Stream<String> textFile) throws IOException {
		// Rip the clusters from the lines and parse them to integers.
		return textFile.map(MFNDBenchmark::parseLine);
	}

	private static int[] parseLine(String line) {
		String[] datapoints = line.trim().split(" ");
		int[] intPoints = new int[datapoints.length];
		try {
			for (int i = 0; i < datapoints.length; i++)
				intPoints[i] = Integer.parseInt(datapoints[i]);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return intPoints;
	}

	private static Stream<String> downloadLines(String urlString) throws IOException {
		URL url = new URL(urlString);
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		Stream<String> textFile = reader.lines();
		return textFile;
	}
}
