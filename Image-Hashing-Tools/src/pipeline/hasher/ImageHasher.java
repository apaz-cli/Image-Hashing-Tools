
package pipeline.hasher;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import hash.IHashAlgorithm;
import hash.ImageHash;
import pipeline.sources.ImageSource;
import pipeline.sources.impl.loader.ImageLoader;

public class ImageHasher {

	// This is the pool for hashing, different from the one for downloading.
	private ExecutorService pool = Executors.newWorkStealingPool(3);

	private ImageSource source;
	private IHashAlgorithm algorithm;
	private Object output;
	private final int outputType = -1;

	/**
	 * Inputs: File (If folder, construct image source. If file, construct reader.
	 * If DNE, throw exception.) Collection of BufferedImage, SourcedImage, or
	 * IImage<?>, or just the one image ImageSource URL
	 * 
	 * Outputs: File or Collection of ImageHash or String PrintWriter or PrintStream
	 * Appendable?
	 */
	public ImageHasher(Object source, IHashAlgorithm algorithm, Object output) throws IllegalArgumentException {

		// Create ImageSource
		ImageSource src = null;
		if (source instanceof ImageSource) {
			src = (ImageSource) source;
		} else if (source instanceof File) {
			File s = (File) source;
			boolean readable = s.canRead();
			if (!readable) {
				throw new IllegalArgumentException("The source file must be a readable directory. Readable: " + readable
						+ ". If the file is not readable, this could possibly be because it does not exist, or may alternatively be due to insufficient permissions.");
			}
			src = new ImageLoader(s);
		} else if (source instanceof Collection<?>) {
			
		}

		// Writers
		this.source = src;
		this.algorithm = algorithm;
		this.setOutputType(output);
		this.output = output;
	}

	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, Object output) {
		this.source = source;
		this.algorithm = algorithm;
		this.setOutputType(output);
		this.output = output;
	}

	public void hashAll() {

	}
	
	private void setOutputType(Object output) {

	}

	void recieveHash(ImageHash hash, String source) {

	}

	

}
