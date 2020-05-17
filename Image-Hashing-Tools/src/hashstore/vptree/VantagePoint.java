package hashstore.vptree;

import java.util.List;

interface VantagePoint<T extends MetricComparable<? extends T>> {
	
	abstract List<T> getAllChildren();

	abstract List<T> getAllAndDestroy();

	abstract void destroy();
}
