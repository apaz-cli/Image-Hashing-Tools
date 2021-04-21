package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class is used in the implementation of LinearHashStore.
 * 
 * @author apaz
 * @param <K> The type of data being stored.
 */
@SuppressWarnings("serial")
public class NNList<K> extends ArrayList<K> {
	int k;
	Comparator<K> c;

	public NNList(int k, Comparator<K> c) {
		super();
		this.k = k;
		this.c = c;
	}

	@Override
	public boolean add(K e) {
		synchronized (this) {
			return super.add(e);
		}
	}

	public K getWorstNN() {
		justify();
		return this.isEmpty() ? null : this.get(this.size() - 1);
	}

	public List<K> kNN(int k) {
		justify();
		synchronized (this) {
			if (this.size() < k) return this;
			return this.isEmpty() ? null : new ArrayList<>(this.subList(0, k));
		}
	}

	public void justify() {
		Collections.sort(this, c);
		if (this.size() > k) this.subList(k, this.size()).clear();
	}

}
