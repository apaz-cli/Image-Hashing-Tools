package hashstore.vptree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

// An ArrayList with no duplicates
class VPLeaf<T extends MetricComparable<? extends T>> extends ArrayList<T> implements VantagePoint<T> {

	private static final long serialVersionUID = -2592139339516215386L;

	// Class is not public, so these are not actually visible
	VPLeaf() {
		super();
	}

	VPLeaf(Collection<T> l) {
		super(new HashSet<T>(l));
	}

	@Override
	public boolean add(T element) {
		if (this.contains(element)) {
			return false;
		} else {
			return super.add(element);
		}
	}

	@Override
	public void add(int index, T element) {
		if (this.contains(element)) {
			return;
		} else {
			super.add(index, element);
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> datalist) {
		ArrayList<T> l = new ArrayList<>(datalist);
		l.removeAll(this);
		return super.addAll(l);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> collection) {
		ArrayList<T> l = new ArrayList<>(collection);
		l.removeAll(this);
		return super.addAll(index, l);
	}
	

}
