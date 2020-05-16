package pipeline.sources.joined;

import java.util.Collection;
import java.util.Vector;

public class CircularVector<E> extends Vector<E> {

	private static final long serialVersionUID = 8997841901543318476L;

	public CircularVector() {
		super();
	}

	public CircularVector(Collection<? extends E> c) {
		super(c);
	}

	public CircularVector(int initialCapacity) {
		super(initialCapacity);
	}

	public CircularVector(int initialCapacity, int capacityIncrement) {
		super(initialCapacity, capacityIncrement);
	}

	@Override
	public E get(int i) {
		return super.get(i % this.size());
	}
}
