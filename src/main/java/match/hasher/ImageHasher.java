
package match.hasher;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import hash.IHashAlgorithm;
import hash.ImageHash;
import match.downloader.ImageDownloader;

public class ImageHasher {

	// This is the pool for hashing, not the one for downloading.
	private static ExecutorService pool = Executors.newWorkStealingPool(20);

	// Provides a constant stream of images to hash.
	private ImageDownloader downloader;

	// Either writes downloads to file
	private BufferedWriter writer;

	// or if writer is null appends hashes to list.
	private volatile ArrayList<ImageHash> hashList;

	// The specific hash being used
	private IHashAlgorithm algorithm;

	private Boolean finished;

	// TODO build other constructors which can take images in other ways.
	// Maybe make a variation where if the urls file (rename this) is a directory,
	// you recursively search through all subdirectories, ripping everything that
	// looks like an image.

	// Hashes file can be null. If so, build a list.
	public ImageHasher(File urls, int bufferSize, IHashAlgorithm algorithm, File hashes)
			throws MalformedURLException, IOException, InterruptedException, ExecutionException {

		this.downloader = new ImageDownloader(urls, bufferSize);

		// Decide to use writer or list
		this.writer = hashes != null ? new BufferedWriter(new FileWriter(hashes)) : null;
		this.hashList = this.writer == null ? new ArrayList<>() : null;

		this.algorithm = algorithm;
		this.finished = false;
		this.beginHashing();
	}

	public ImageHasher(File urls, IHashAlgorithm algorithm, File hashes)
			throws MalformedURLException, IOException, InterruptedException, ExecutionException {
		this(urls, 25, algorithm, hashes);
	}

	private void beginHashing() throws IOException, InterruptedException {

		// Submit tasks to the pool to be hashed when the images become available.
		BufferedImage next;
		while ((next = downloader.getNextImage()) != null) {
			synchronized (pool) {
				pool.execute(new HashTask(this, next, this.algorithm));
			}
		}
		// Seamlessly release and rebuild pool. This (shouldn't) affect other instances
		// of this class sharing the same threadpool.
		synchronized (pool) {
			pool.shutdown();
			pool.awaitTermination(7, TimeUnit.DAYS);
			pool = Executors.newWorkStealingPool(20);
		}

		writer.flush();
		writer.close();

		// Inform those awaiting list completion
		synchronized (finished) {
			finished = true;
			finished.notifyAll();
		}
	}

	void recieveHash(ImageHash hash) {
		// If we're writing to file, do so.
		if (writer != null) {
			synchronized (writer) {
				try {
					writer.write(hash + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// If we're appending to list, do that instead
		else {
			synchronized (hashList) {
				hashList.add(hash);
			}
		}
	}

	// Returns null if already saved images to file
	public List<ImageHash> awaitListCompletion() {
		// Await the hashing (and storing of hashes) of all images
		try {
			synchronized (finished) {
				if (!finished) {
					finished.wait();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Return null if writing to file, or list of hashes
		return hashList;
	}

}
