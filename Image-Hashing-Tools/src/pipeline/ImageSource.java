package pipeline;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import hash.IHashAlgorithm;
import hash.ImageHash;
import image.IImage;
import image.implementations.SourcedImage;

public interface ImageSource extends Spliterator<SourcedImage> {

	/************/
	/* Abstract */
	/************/

	@Override
	public abstract int characteristics();

	@Override
	public abstract long estimateSize();

	// Returns null if no more remain, otherwise fetches it on the same thread.
	public abstract SourcedImage next();

	@Override
	public abstract Spliterator<SourcedImage> trySplit();
	
	public abstract String getSourceName();

	/***********/
	/* Default */
	/***********/

	@Override
	public default boolean tryAdvance(Consumer<? super SourcedImage> action) {
		SourcedImage img = this.next();
		if (img == null) return false;
		action.accept(img);
		return true;
	}

	public default Stream<SourcedImage> stream() { return StreamSupport.stream(this, false); }

	public default Stream<SourcedImage> parallelStream() { return StreamSupport.stream(this, true); }

	public default Stream<ImageHash> streamHashes(IHashAlgorithm alg) {
		return this.stream().map(img -> alg.hash(img));
	}

	public default Stream<ImageHash> parallelStreamHashes(IHashAlgorithm alg) {
		return this.stream().map(img -> alg.hash(img));
	}

	public default BufferedImage nextBufferedImage() throws NoSuchElementException {
		return this.next().toBufferedImage();
	}

	public default IImage<?> nextIImage() throws NoSuchElementException { return this.next().unwrap(); }

	public default List<SourcedImage> toList() { return this.parallelStream().collect(Collectors.toList()); }

	public default List<IImage<?>> toIImageList() {
		return this.parallelStream().map(i -> i.unwrap()).collect(Collectors.toList());
	}

	public default List<BufferedImage> toBufferedImageList() {
		return this.parallelStream().map(i -> i.toBufferedImage()).collect(Collectors.toList());
	}

	public default void forEachRemainingInParallel​(Consumer<? super SourcedImage> action) {
		this.parallelStream().forEach(action);
	}

}
