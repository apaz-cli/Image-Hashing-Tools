package hashstore.vptree;

// A class that implements MetricComparable can be treated as a point on a metric space.
public interface MetricComparable<T extends MetricComparable<? extends T>> {
	
	abstract double distance(T other);

	default boolean within(double threshold, T other) {
		return this.distance(other) < threshold;
	}
}
