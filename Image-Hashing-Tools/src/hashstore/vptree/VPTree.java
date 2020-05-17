package hashstore.vptree;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Vector;
import java.util.stream.Collectors;

import javafx.util.Pair;

public class VPTree<T extends MetricComparable<? extends T>> extends AbstractCollection<T> {

	private VPNode<T> root;
	private int size;
	private int leafCapacity;

	public VPTree(Collection<T> datapoints) {
		this(new ArrayList<T>(datapoints));
	}

	public VPTree(List<T> datapoints) {
		this(datapoints, 100);
	}

	public VPTree(List<T> datapoints, int leafCapacity) {
		this.root = new VPNode<>(datapoints);
		this.size = datapoints.size();
		this.leafCapacity = leafCapacity;
	}

	// HashStore method
	public List<T> toList() {
		return this.root.getAllChildren();
	}

	public VPTree<T> rebuild() {
		return new VPTree<>(this.toList(), 25);
	}

	public synchronized List<T> kNN(T query, int k) {
		@SuppressWarnings("unchecked")
		final MetricComparable<T> target = (MetricComparable<T>) query;
		if (k <= 0) throw new IllegalArgumentException("k cannot be negative or zero. Got: " + k);
		if (k > this.size) return new ArrayList<T>();

		// This queue is sorted backwards, such that the farthest node from the query
		// point is first.
		PriorityQueue<Pair<T, Double>> NNs = new PriorityQueue<Pair<T, Double>>(k,
				(first, second) -> { return Double.compare(second.getValue(), first.getValue()); });

		double tau = 0; // The distance from the farthest nearest neighbor to the query point. We keep
						// track of this because we don't want to traverse sections of the tree that we
						// don't need to.

		// Keep track of the nodes which we need to visit.
		final Stack<VantagePoint<T>> toTraverse = new Stack<>();
		toTraverse.push(this.root);

		// Traverse the tree for the neighbors
		VantagePoint<T> currentVantagePoint = null;
		while ((currentVantagePoint = toTraverse.isEmpty() ? null : toTraverse.pop()) != null) {

			// Until we hit a leaf, keep traversing.
			if (currentVantagePoint instanceof VPNode<?>) {
				VPNode<T> currentNode = (VPNode<T>) currentVantagePoint;
				final double radius = currentNode.radius;
				final double distCurrentToQuery = target.distance(currentNode.data);

				// Update tau, the distance to the farthest nearest neighbor away, and add it as
				// a NN if applicable
				NNs.add(new Pair<T, Double>(currentNode.data, distCurrentToQuery));
				while (NNs.size() > k) {
					NNs.remove();
				}
				tau = NNs.peek().getValue();

				// Determine where to traverse inner, outer, or both
				if (distCurrentToQuery < radius - tau) { // No overlap, entirely inside
					toTraverse.push(currentNode.innerChild);
				} else {
					if (distCurrentToQuery > radius + tau) { // No overlap, entirely outside
						toTraverse.push(currentNode.outerChild);
					} else { // The hyperspheres do overlap, so search both
						toTraverse.push(currentNode.innerChild);
						toTraverse.push(currentNode.outerChild);
					}
				}
			}

			// Once we hit a leaf, add the whole list of data and if the stack is empty then
			// proceed on
			else {
				VPLeaf<T> currentLeaf = (VPLeaf<T>) currentVantagePoint;
				NNs.addAll(currentLeaf.getAllChildren().stream()
						.map((t) -> { return new Pair<T, Double>(t, target.distance(t)); })
						.collect(Collectors.toList()));
			}
		}

		// Place into a list, sort (because we must end with a leaf which is inserted
		// unsorted)
		List<Pair<T, Double>> NNsList = new ArrayList<>(NNs);
		Collections.sort(NNsList, (p1, p2) -> Double.compare(p1.getValue(), p2.getValue()));
		return NNsList.subList(0, k).stream().map(p -> p.getKey()).collect(Collectors.toList());
	}

	// Duplicates not permitted
	@Override
	public synchronized boolean add(T datapoint) {
		@SuppressWarnings("unchecked")
		MetricComparable<T> target = (MetricComparable<T>) datapoint;

		return true;
	}

	@Override
	public synchronized void clear() {
		this.size = 0;
		this.root = null;
	}

	@Override
	public boolean contains(Object o) {

		@SuppressWarnings("unchecked") // Throw ClassCastException if impossible conversion.
		MetricComparable<T> target = (MetricComparable<T>) o;

		// Traverse the tree until you find a leaf
		VantagePoint<T> currentVantagePoint = this.root;
		while (currentVantagePoint != null) {

			if (currentVantagePoint instanceof VPNode<?>) {
				VPNode<T> current = (VPNode<T>) currentVantagePoint;
				T data = current.data;

				if (data.equals(o)) return true;

				if (target.within(current.radius, data)) {
					currentVantagePoint = current.innerChild;
				} else {
					currentVantagePoint = current.outerChild;
				}

			} else { // is leaf
				return ((VPLeaf<?>) currentVantagePoint).contains(o);
			}

		}

		return false;
	}

	// Inherit Javadoc
	@Override
	@SuppressWarnings({ "unchecked" })
	public synchronized boolean remove(Object o) {

		// Throw ClassCastException if impossible conversion.
		MetricComparable<T> target = (MetricComparable<T>) o;
		
		this.size--;
		return true;
	}

	@Override
	public synchronized int size() {
		return this.size;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> c) {
		return addAllToVantagePoint(c, this.root);
	}
	
	public synchronized boolean addAllToVantagePoint(Collection<? extends T> c, VantagePoint<T> vp) {
		return false;
	}
	
	@Override
	public synchronized Iterator<T> iterator() {
		return null;
	}
}
