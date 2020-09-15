package utils;

public class Triple<T, U, V> {

	private final T first;
	private final U second;
	private final V third;

	public Triple(T first, U second, V third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public T getFirst() { return first; }

	public U getSecond() { return second; }

	public V getThird() { return third; }

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (!(o instanceof Triple<?, ?, ?>)) return false;
		Triple<T, U, V> other = null;
		try {
			other = (Triple<T, U, V>) o;
		} catch (ClassCastException e) {
			return false;
		}
		return this.first.equals(other.first) && this.second.equals(other.second) && this.third.equals(other.third);
	}

	@Override
	public String toString() {
		return new StringBuilder().append('<')
				.append(first.toString()).append(',').append(second.toString()).append(',').append(third.toString())
				.append('>').toString();
	}

}
