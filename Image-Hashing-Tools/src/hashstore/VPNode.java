package hashstore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import image.PixelUtils;

// This class is not to be used outside of the VPTree code. No unnecessary robustness checks.
class VPNode<T extends MetricComparable<? extends T>> {

	static Random r = new Random();

	VPNode(T datapoint) {
		this.data = datapoint;
		this.radius = 0.0;
		this.innerChild = null;
		this.outerChild = null;
	}

	@SuppressWarnings("unchecked")
	VPNode(List<T> workingPartition) throws IllegalArgumentException {
		PixelUtils.assertNotNull("workingPartition", workingPartition);
		if (workingPartition.isEmpty()) throw new IllegalArgumentException("Working partition may not be empty.");
		if (workingPartition.size() == 1) {
			this.data = workingPartition.get(0);
			this.radius = 0.0;
			this.innerChild = null;
			this.outerChild = null;
			return;
		}

		// Let the root be a random element from the list.
		this.data = chooseVantagePoint(workingPartition);

		// Calculate the median distance between the chosen point and the rest of the
		// points in the set.
		{
			double[] distances = new double[workingPartition.size()];
			for (int i = 0; i < workingPartition.size(); i++) {
				distances[i] = ((MetricComparable<T>) this.data).distance((T) workingPartition.get(i));
			}
			Arrays.sort(distances);
			this.radius = distances.length % 2 == 0 ? distances[distances.length / 2]
					: (distances[distances.length / 2] + distances[(distances.length / 2) + 1]) / 2;
		}

		// Partition the data into near and far.
		boolean parallel = workingPartition.size() > 500;
		final List<T> innerChildPartition = parallel ? new Vector<>() : new ArrayList<>(),
				outerChildPartition = parallel ? new Vector<>() : new ArrayList<>();
		{
			Stream<T> partStream = parallel ? workingPartition.parallelStream() : workingPartition.stream();
			partStream.forEach((t) -> {
				if (((MetricComparable<T>) this.data).within(this.radius, t)) {
					innerChildPartition.add(t);
				} else {
					outerChildPartition.add(t);
				}
			});
		}

		if (parallel) {
			try {
				ExecutorService pool = Executors.newFixedThreadPool(2);
				pool.execute(() -> {
					this.innerChild = new VPNode<T>(innerChildPartition);
					innerChildPartition.clear(); 
				});
				pool.execute(() -> {
					this.outerChild = new VPNode<T>(outerChildPartition);
					outerChildPartition.clear();
				});
				pool.shutdown();
				pool.awaitTermination(7, TimeUnit.DAYS);
			} catch (InterruptedException e) {
			}

		} else {
			this.innerChild = new VPNode<T>(innerChildPartition);
			innerChildPartition.clear();
			this.outerChild = new VPNode<T>(outerChildPartition);
			outerChildPartition.clear();
		}

	}

	private T chooseVantagePoint(List<T> workingPartition) {
		return workingPartition.remove(r.nextInt(workingPartition.size()));
	}

	private T data;
	private VPNode<T> innerChild;
	private VPNode<T> outerChild;
	private double radius;
	// Children may be null

	T getData() {
		return this.data;
	}

	VPNode<T> getInner() {
		return this.innerChild;
	}

	VPNode<T> getOuter() {
		return this.outerChild;
	}

	void setInner(VPNode<T> newInner) {
		this.innerChild = newInner;
	}

	void setOuter(VPNode<T> newOuter) {
		this.outerChild = newOuter;
	}

	double getRadius() {
		return this.radius;
	}

	@SuppressWarnings("unchecked")
	void addNodeToLeafNode(T datapoint) {
		boolean hasInner = this.innerChild == null, hasOuter = this.outerChild == null;
		if (hasInner && hasOuter)
			throw new IllegalArgumentException("This node is not a leaf node.");
		else if (!(hasInner || hasOuter)) {
			this.outerChild = new VPNode<T>(datapoint);
			this.radius = ((MetricComparable<T>) datapoint).distance(this.data);
		} else {
			VPNode<T> newSelf = new VPNode<>(destroySelf());
			this.data = newSelf.getData();
			this.innerChild = newSelf.getInner();
			this.outerChild = newSelf.getOuter();
			this.radius = newSelf.getRadius();
		}

	}

	void addSelfAndChildren(Collection<T> coll) {
		coll.add(this.data);
		if (this.innerChild != null) {
			this.innerChild.addSelfAndChildren(coll);
		}
		if (this.outerChild != null) {
			this.outerChild.addSelfAndChildren(coll);
		}
	}

	List<T> destroySelf() {
		ArrayList<T> elements = new ArrayList<>();
		this.addSelfAndChildrenAndDestroy(elements);
		return elements;
	}

	// Severs the tree at this node, allowing GC to occur on the detached subtrees
	// after the method returns.
	void addSelfAndChildrenAndDestroy(Collection<T> coll) {
		coll.add(this.data);
		this.data = null;

		if (this.innerChild != null) {
			this.innerChild.addSelfAndChildrenAndDestroy(coll);
		}
		this.innerChild = null;

		if (this.outerChild != null) {
			this.outerChild.addSelfAndChildrenAndDestroy(coll);
		}
		this.outerChild = null;
	}
}
