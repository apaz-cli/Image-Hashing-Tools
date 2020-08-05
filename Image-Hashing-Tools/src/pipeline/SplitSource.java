package pipeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;

import image.implementations.SourcedImage;

public class SplitSource implements ImageSource {

	List<File> files = new ArrayList<>();

	public SplitSource(List<File> files) {
		Objects.nonNull(files);
		this.files = files;
	}

	@Override
	public int characteristics() {
		return CONCURRENT | IMMUTABLE | NONNULL;
	}

	@Override
	public synchronized long estimateSize() {
		return files.size();
	}

	@Override
	public SourcedImage next() {
		if (files.isEmpty()) return null;
		File f = files.remove(files.size() - 1);
		try {
			return new SourcedImage(f);
		} catch (IOException e) {
			return this.next();
		}
	}

	@Override
	public synchronized Spliterator<SourcedImage> trySplit() {
		if (files.size() < 50) return null;

		int size = files.size();
		List<File> first = new ArrayList<>(files.subList(0, (size + 1) / 2));
		List<File> second = new ArrayList<>(files.subList((size + 1) / 2, size));

		this.files = first;
		return new SplitSource(second);
	}

}
