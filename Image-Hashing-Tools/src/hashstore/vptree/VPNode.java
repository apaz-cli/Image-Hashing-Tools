package hashstore.vptree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import image.PixelUtils;

// This class is not to be used outside of the VPTree code. No unnecessary robustness checks.
class VPNode<T extends MetricComparable<? extends T>> implements VantagePoint<T> {

	private static Random r = new Random();

	// Class is not public, so these are not actually visible
	T data;
	VantagePoint<T> innerChild;
	VantagePoint<T> outerChild;
	double radius;

	VPNode(T datapoint, double radius, VantagePoint<T> innerChild, VantagePoint<T> outerChild) {
		this.data = datapoint;
		this.radius = radius;
		this.innerChild = innerChild;
		this.outerChild = outerChild;
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
		// Chooses at random. This requires more research.
		return workingPartition.remove(r.nextInt(workingPartition.size()));
	}

	@Override
	public List<T> getAllChildren() {
		ArrayList<T> children = new ArrayList<>();

		Stack<VantagePoint<T>> toTraverse = new Stack<>();
		VantagePoint<T> currentVantagePoint;
		toTraverse.push(this);

		while (!toTraverse.isEmpty()) {
			currentVantagePoint = toTraverse.pop();
			if (currentVantagePoint instanceof VPNode<?>) {
				VPNode<T> vp = ((VPNode<T>) currentVantagePoint);
				children.add(vp.data);
				toTraverse.push(vp.innerChild);
				toTraverse.push(vp.outerChild);
			} else if (currentVantagePoint instanceof VPLeaf<?>) {
				children.addAll(((VPLeaf<T>) currentVantagePoint).getAllChildren());
			}
		}

		return children;
	}

	@Override
	public List<T> getAllAndDestroy() {
		ArrayList<T> children = new ArrayList<>();

		Stack<VantagePoint<T>> toTraverse = new Stack<>();
		VantagePoint<T> currentVantagePoint;
		toTraverse.push(this);

		while (!toTraverse.isEmpty()) {
			currentVantagePoint = toTraverse.pop();
			if (currentVantagePoint instanceof VPNode<?>) {
				VPNode<T> vp = ((VPNode<T>) currentVantagePoint);
				children.add(vp.data);
				toTraverse.push(vp.innerChild);
				toTraverse.push(vp.outerChild);
				vp.data = null;
				vp.innerChild = null;
				vp.outerChild = null;
			} else if (currentVantagePoint instanceof VPLeaf<?>) {
				VPLeaf<T> leaf = (VPLeaf<T>) currentVantagePoint;
				children.addAll(leaf.getAllChildren());
				leaf.data = null;
				leaf.leafNumber = -1;

			}
		}

		return children;
	}

	@Override
	public void destroy() {
		Stack<VantagePoint<T>> toTraverse = new Stack<>();
		VantagePoint<T> currentVantagePoint;
		toTraverse.push(this);

		while (!toTraverse.isEmpty()) {
			currentVantagePoint = toTraverse.pop();
			if (currentVantagePoint instanceof VPNode<?>) {
				VPNode<T> vp = ((VPNode<T>) currentVantagePoint);
				toTraverse.push(vp.innerChild);
				toTraverse.push(vp.outerChild);
				vp.data = null;
				vp.innerChild = null;
				vp.outerChild = null;
			} else if (currentVantagePoint instanceof VPLeaf<?>) {
				VPLeaf<T> leaf = (VPLeaf<T>) currentVantagePoint;
				leaf.data = null;
				leaf.leafNumber = -1;

			}
		}
	}

}
