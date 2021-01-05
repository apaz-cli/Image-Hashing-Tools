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
}
