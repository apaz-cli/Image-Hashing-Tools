package hashstore;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import hash.IHashAlgorithm;
import hash.ImageHash;
import image.PixelUtils;
import pipeline.dedup.HashMatch;
import utils.NNList;
import utils.Pair;

public class LinearHashStore implements HashStore, Closeable {

	File textFile;
	PrintWriter writer;
	IHashAlgorithm alg;

	public LinearHashStore(File textFile) throws IOException {
		PixelUtils.assertNotNull("textFile", textFile);
		File parent = textFile.getParentFile();
		if (parent != null) {
			if (!parent.exists()) {
				textFile.getParentFile().mkdirs();
			}
		}
		if (!textFile.exists()) {
			textFile.createNewFile();
		}
		if (!textFile.canRead() || !textFile.canWrite()) {
			throw new IOException("Cannot either read or write to the file.");
		}

		this.textFile = textFile;
		this.writer = new PrintWriter(new FileWriter(textFile.getPath(), true));
	}

	private BufferedReader makeReader() throws FileNotFoundException {
		return new BufferedReader(new FileReader(textFile));
	}

	@Override
	public synchronized ImageHash NN(ImageHash query) throws IOException {
		double nearestDistance = Double.MAX_VALUE;
		ImageHash nearest = null;

		BufferedReader r = makeReader();
		try {
			String line;
			while ((line = r.readLine()) != null) {
				ImageHash lineHash = ImageHash.fromString(line);
				double dist = query.distance(lineHash);
				if (dist < nearestDistance) {
					nearest = lineHash;
					nearestDistance = dist;
				}
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} finally {
			r.close();
		}

		return nearest;
	}

	@Override
	public synchronized List<ImageHash> kNN(ImageHash query, int k) throws IOException {

		double worstNN = Double.MAX_VALUE;
		NNList<Pair<ImageHash, Double>> NNs = new NNList<Pair<ImageHash, Double>>(k,
				(first, second) -> { return Double.compare(first.getValue(), second.getValue()); });

		BufferedReader r = makeReader();
		try {
			String line;
			while ((line = r.readLine()) != null) {
				ImageHash lineHash = ImageHash.fromString(line);
				double dist = query.distance(lineHash);
				if (dist <= worstNN) {
					NNs.add(new Pair<>(lineHash, dist));
					worstNN = NNs.getWorstNN().getValue();
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			r.close();
		}

		NNs.justify();
		return new ArrayList<>(NNs.stream().map(pair -> pair.getKey()).collect(Collectors.toList()));
	}

	@Override
	public synchronized List<ImageHash> allWithinDistance(ImageHash h, double distance) throws IOException {
		List<ImageHash> inDistance = new ArrayList<>();

		BufferedReader r = makeReader();
		try {
			String line;
			while ((line = r.readLine()) != null) {
				ImageHash lineHash = ImageHash.fromString(line);
				double dist = h.distance(lineHash);
				if (dist <= distance) {
					inDistance.add(lineHash);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} finally {
			r.close();
		}

		return inDistance;
	}

	@Override
	public synchronized List<ImageHash> toList() throws IOException {
		List<ImageHash> l = new ArrayList<>();
		BufferedReader r = makeReader();
		String line;
		while ((line = r.readLine()) != null) {
			try {
				l.add(ImageHash.fromString(line));
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
		}
		r.close();
		return l;
	}

	@Override
	public synchronized void store(ImageHash hash) {
		writer.println(hash);
		writer.flush();
	}

	@Override
	public synchronized void storeAll(Collection<? extends ImageHash> hashes) {
		if (this.alg == null) for (ImageHash h : hashes) {
			writer.println(h);
		}
		writer.flush();
	}

	@Override
	public synchronized void close() throws IOException {
		this.writer.close();
	}

	@Override
	public List<HashMatch> findMatches(int atATime) throws IOException {

		List<ImageHash> allHashes = new ArrayList<>();
		{
			BufferedReader reader = makeReader();
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					try {
						allHashes.add(ImageHash.fromString(line));
					} catch (ClassNotFoundException e) {
						throw new IOException(e);
					}
				}
			} finally {
				reader.close();
			}
		}
		if (allHashes.isEmpty()) return new ArrayList<>();
		this.alg = allHashes.get(0).getAlgorithm();
		final IHashAlgorithm alg = this.alg;
		final List<HashMatch> results = new Vector<>();

		List<Pair<List<ImageHash>, List<ImageHash>>> currentHashesAndRests = new ArrayList<>();

		for (int i = 0; i < allHashes.size(); i += atATime) {
			// Find current position,
			int endIndex = Math.min(allHashes.size(), i + atATime);
			List<ImageHash> currentHashes = allHashes.subList(i, endIndex);
			List<ImageHash> rest = allHashes.subList(endIndex, allHashes.size());

			currentHashesAndRests.add(new Pair<>(currentHashes, rest));
		}

		// Try to find duplicates inside each of the sublists by n^2 brute force.
		currentHashesAndRests.parallelStream().map(currentPartitionAndRest -> currentPartitionAndRest.getKey())
				.forEach(currentHashes -> {
					for (int y = 0; y < currentHashes.size(); y++) {
						for (int z = y + 1; z < currentHashes.size(); z++) {
							if (currentHashes.get(y).getSource().equals(currentHashes.get(z).getSource())) continue;
							if (alg.matches(currentHashes.get(y), currentHashes.get(z)))
								results.add(new HashMatch(currentHashes.get(y), currentHashes.get(z)));
						}
					}
				});

		// Compare each of the (now internally consistent) list of hashes to the rest of
		// the hashes, all at once and in parallel.
		currentHashesAndRests.parallelStream().forEach(currentHashesAndRest -> {
			final List<ImageHash> currentHashes = currentHashesAndRest.getKey();
			final List<ImageHash> rest = currentHashesAndRest.getValue();
			rest.stream().forEach(h -> {
				for (ImageHash c : currentHashes) {
					if (h.getSource().equals(c.getSource())) continue;
					if (alg.matches(h, c)) results.add(new HashMatch(h, c));
				}
			});
		});

		return results;
	}

}
