package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@SuppressWarnings("serial")
public class NNList<K> extends ArrayList<K> {
	int k;
	Comparator<K> c;

	public NNList(int k, Comparator<K> c) {
		super();
		this.k = k;
		this.c = c;
	}

	public K getWorstNN() {
		justify();
		return this.isEmpty() ? null : this.get(this.size() - 1);
	}

	public void justify() {
		Collections.sort(this, c);
		if (this.size() > k) this.subList(k, this.size()).clear();
	}

}
