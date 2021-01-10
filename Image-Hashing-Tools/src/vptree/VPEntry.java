package vptree;

public class VPEntry<T> {
	public T item;
	public double distance;

	public VPEntry(T item, double distance) {
		this.item = item;
		this.distance = distance;
	};

	@Override
	public String toString() {
		return new StringBuilder()
				.append('<')
				.append(this.item.toString())
				.append(", ")
				.append(this.distance)
				.append('>')
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof VPEntry<?>)) return false;
		VPEntry<?> other = (VPEntry<?>) o;
		return this.item.equals(other.item) && (this.distance == other.distance);
	}
}
