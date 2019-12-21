
package pipeline.hasher;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import hash.IHashAlgorithm;
import pipeline.sources.ImageSource;
import pipeline.sources.impl.collection.ImageCollection;
import pipeline.sources.impl.loader.ImageLoader;

public class ImageHasher {

	// This is the pool for hashing, different from the one for downloading.
	private ExecutorService pool = Executors.newWorkStealingPool(3);

	private ImageSource source;
	private IHashAlgorithm algorithm;
	private Object output;

	// -1: none
	// 0: PrintStream (includes System.out, System.err)
	// 1: File
	// 2: Collection<ImageHash>
	// 3: PrintWriter
	private int outputType = -1;

	public ImageHasher(ImageSource source, IHashAlgorithm algorithm, Object output) {
		this((Object) source, algorithm, output);
	}

	/**
	 * Inputs: File (If folder, construct image source. If file, construct reader.
	 * If DNE, throw exception.) Collection of BufferedImage, SourcedImage, or
	 * IImage<?>, or just the one image ImageSource URL
	 * 
	 * Outputs: File or Collection of ImageHash or String PrintWriter or PrintStream
	 * Appendable?
	 */
	public ImageHasher(Object input, IHashAlgorithm algorithm, Object output) throws IllegalArgumentException {
		this.source = createSource(input);
		this.algorithm = algorithm;
		this.output = output;
		this.setOutputType();
	}

	private ImageSource createSource(Object input) {
		// Create ImageSource out of input
		ImageSource src = null;
		if (input instanceof ImageSource) {
			src = (ImageSource) input;
		} else if (input instanceof File) {
			File s = (File) input;
			boolean readable = s.canRead();
			if (!readable) {
				throw new IllegalArgumentException("The source file must be a readable directory. Readable: " + readable
						+ ". If the file is not readable, this could possibly be because it does not exist, "
						+ "or may alternatively be due to insufficient permissions. "
						+ "If you want to read only one file, use ImageUtils.openImage.");
			}
			src = new ImageLoader(s);
		} else if (input instanceof Collection<?>) {
			src = new ImageCollection((Collection<?>) input);
		}
		return src;
	}

	private void setOutputType() {
		if (this.output instanceof PrintStream) {
			this.outputType = 0;
		} else if (this.output instanceof File) {
			this.outputType = 1;
		} else if (this.output instanceof Collection<?>) {
			this.outputType = 2;
		} else if (this.output instanceof PrintWriter) {
			this.outputType = 3;
		} else {
			throw new UnsupportedOperationException(
					"ImageHasher does not know how to use output: " + this.output.toString());
		}
	}

	private void outputPrintStream() {

	}

	private void outputFile() {

	}

	private void outputCollection() {

	}

	private void outputPrintWriter() {
		
	}

	public void hash() {
		
	}

	public void hashSourced() {

	}

	public void hashAll() {

	}

	public void hashAllSourced() {

	}

}
