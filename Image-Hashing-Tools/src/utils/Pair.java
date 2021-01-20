package utils;

public class Pair<K, V> {
	private final K key;
	private final V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public V getValue() { return value; }

	public K getKey() { return key; }

	public boolean contains(Object o) {
		return this.key.equals(o) || this.value.equals(o);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (!(o instanceof Pair<?, ?>)) return false;
		Pair<K, V> other = null;
		try {
			other = (Pair<K, V>) o;
		} catch (ClassCastException e) {
			return false;
		}
		return this.key.equals(other.getKey()) && this.value.equals(other.getValue());
	}

	@Override
	public int hashCode() {
		return key.hashCode() ^ value.hashCode();
	}

	@Override
	public String toString() {
		return new StringBuilder().append('<').append(key.toString()).append(',').append(value.toString()).append('>')
				.toString();
	}

}
