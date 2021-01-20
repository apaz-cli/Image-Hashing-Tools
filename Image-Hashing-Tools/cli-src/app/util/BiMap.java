package app.util;

import java.util.HashMap;
import java.util.List;

import utils.Pair;

public class BiMap<K, V> {

	private HashMap<K, V> forward;
	private HashMap<V, K> backward;

	public BiMap(List<Pair<K, V>> pairs) {
		this.forward = new HashMap<>();
		this.backward = new HashMap<>();
		for (Pair<K, V> p : pairs) {
			this.forward.put(p.getKey(), p.getValue());
			this.backward.put(p.getValue(), p.getKey());
		}
	}

	public V getValue(K key) {
		return forward.get(key);
	}

	public K getKey(V value) {
		return backward.get(value);
	}

}
