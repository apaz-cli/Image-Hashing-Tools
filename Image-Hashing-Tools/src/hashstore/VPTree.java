package hashstore;

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
	private int treeSize;
	private List<T> extras;
	private boolean listAdd;

	public VPTree(Collection<T> datapoints) {
		this(new ArrayList<T>(datapoints));
	}

	public VPTree(List<T> datapoints) {
		this(datapoints, true, 25);
	}

	public VPTree(List<T> datapoints, boolean listAdd, int bucketSize) {
		this.root = new VPNode<>(datapoints);
		this.treeSize = datapoints.size();
		this.extras = new Vector<>();
		this.listAdd = listAdd;
	}

	// HashStore method
	public List<T> toList() {
		List<T> l = new ArrayList<>(extras);
		root.addSelfAndChildren(l);
		return l;
	}

	public VPTree<T> rebuild() {
		return new VPTree<>(this.toList(), this.listAdd, 25);
	}

	public List<T> kNN(T query, int k) {
		@SuppressWarnings("unchecked")
		MetricComparable<T> target = (MetricComparable<T>) query;
		if (k <= 0) throw new IllegalArgumentException("k cannot be negative or zero. Got: " + k);
		if (k > this.treeSize) return new ArrayList<T>();

		// This queue is sorted backwards, such that the farthest node from the query
		// point is first.
		PriorityQueue<Pair<T, Double>> NNs = new PriorityQueue<Pair<T, Double>>(k,
				(first, second) -> { return Double.compare(second.getValue(), first.getValue()); });

		double tau = 0; // The distance from the farthest nearest neighbor to the query point. We keep
						// track of this because we don't want to traverse sections of the tree that we
						// don't need to.

		// Keep track of the nodes which we need to visit.
		final Stack<VPNode<T>> toTraverse = new Stack<>();
		toTraverse.push(this.root);

		// Traverse the tree for the neighbors
		VPNode<T> currentVantagePoint = null;
		while ((currentVantagePoint = toTraverse.isEmpty() ? null : toTraverse.pop()) != null) {

			// Push the current node onto the list and trim the smallest
			final double radius = currentVantagePoint.getRadius();
			final double distCurrentToQuery = target.distance(currentVantagePoint.getData());

			// Update tau, the distance to the farthest nearest neighbor away
			NNs.add(new Pair<T, Double>(currentVantagePoint.getData(), distCurrentToQuery));
			if (NNs.size() == k) {
				NNs.remove();
				tau = NNs.peek().getValue();
			} else if (distCurrentToQuery > tau) {
				tau = distCurrentToQuery;
			}

			// Determine where to traverse inner, outer, or both
			if (distCurrentToQuery < radius - tau) { // No overlap, entirely inside
				VPNode<T> inner = currentVantagePoint.getInner();
				if (inner != null) toTraverse.push(inner);
			} else {
				if (distCurrentToQuery > radius + tau) { // No overlap, entirely outside
					VPNode<T> outer = currentVantagePoint.getOuter();
					if (outer != null) toTraverse.push(outer);
				} else { // The hyperspheres do overlap, so search both
					VPNode<T> inner = currentVantagePoint.getInner();
					if (inner != null) toTraverse.push(inner);
					VPNode<T> outer = currentVantagePoint.getOuter();
					if (outer != null) toTraverse.push(outer);
				}
			}
		}

		List<Pair<T, Double>> NNsList = new ArrayList<>(NNs);
		Collections.reverse(NNsList);
		return NNsList.stream().map(p -> p.getKey()).collect(Collectors.toList());
	}

	// Duplicates not permitted
	@Override
	public boolean add(T datapoint) {
		return this.listAdd ? this.listInsert(datapoint) : this.treeInsert(datapoint);
	}

	public boolean listInsert(T datapoint) {
		if (extras.contains(datapoint)) return false;
		return extras.add(datapoint);
	}

	public boolean treeInsert(T datapoint) {
		if (extras.contains(datapoint)) return extras.add(datapoint);

		// Implement. Keep in mind that if you find it in the tree at any point, you
		// must return false.

		@SuppressWarnings("unchecked") // Throw ClassCastException if impossible conversion.
		MetricComparable<T> target = (MetricComparable<T>) datapoint;

		// Traverse the tree until you find the datapoint or null
		boolean isInner;
		VPNode<T> currentVantagePoint = this.root, nextVantagePoint;
		while (currentVantagePoint != null) {
			T data = currentVantagePoint.getData();

			// Duplicates are not allowed, so return false if you find it in the tree.
			if (data.equals(datapoint)) return false;

			// If we've found null, that's where we put the new node.
			isInner = target.within(currentVantagePoint.getRadius(), data);
			nextVantagePoint = isInner ? currentVantagePoint.getInner() : currentVantagePoint.getOuter();
			if (nextVantagePoint == null) {
				VPNode<T> newNode = new VPNode<>(datapoint);
				if (isInner) {
					currentVantagePoint.setInner(newNode);
				} else {
					currentVantagePoint.setOuter(newNode);
				}
				return true;
			}
			currentVantagePoint = nextVantagePoint;
		}

		return true;
	}

	@Override
	public boolean contains(Object o) {
		if (extras.contains(o)) return true;

		@SuppressWarnings("unchecked") // Throw ClassCastException if impossible conversion.
		MetricComparable<T> target = (MetricComparable<T>) o;

		// Traverse the tree
		VPNode<T> currentVantagePoint = this.root;
		while (currentVantagePoint != null) {
			T data = currentVantagePoint.getData();

			// If we've arrived, we want to hold on to the value of currentVantagePoint and
			// its parent, and immediately get to work on removing it.
			if (data.equals(o)) return true;

			if (target.within(currentVantagePoint.getRadius(), data)) {
				currentVantagePoint = currentVantagePoint.getInner();

			} else {
				currentVantagePoint = currentVantagePoint.getOuter();
			}
		}

		return false;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public boolean remove(Object o) {
		if (extras.remove(o)) return true;

		// Throw ClassCastException if impossible conversion.
		MetricComparable<T> target = (MetricComparable<T>) o;

		// Traverse the tree
		boolean inParentRadius = true;
		VPNode<T> currentVantagePoint = this.root, currentParent = null;
		while (currentVantagePoint != null) {
			T data = currentVantagePoint.getData();

			// If we've arrived, we want to hold on to the value of currentVantagePoint and
			// its parent, and immediately get to work on removing it.
			if (data.equals(o)) {
				break;
			}

			currentParent = currentVantagePoint;
			if (target.within(currentVantagePoint.getRadius(), data)) {
				currentVantagePoint = currentVantagePoint.getInner();
				inParentRadius = true;
			} else {
				currentVantagePoint = currentVantagePoint.getOuter();
				inParentRadius = false;
			}
		}

		if (currentVantagePoint == null) return false;

		// Rebuild what needs to be rebuilt
		VPNode<T> remade = new VPNode<T>(currentVantagePoint.destroySelf());
		if (inParentRadius) {
			currentParent.setInner(remade);
		} else {
			currentParent.setOuter(remade);
		}

		this.treeSize--;
		return true;
	}

	@Override
	public int size() {
		return this.treeSize + extras.size();
	}

	@Override
	public Iterator<T> iterator() {
		final class DoubleIterator implements Iterator<T> {
			private Iterator<T> first, second;

			DoubleIterator(Iterator<T> first, Iterator<T> second) {
				this.first = first;
				this.second = second;
			}

			@Override
			public boolean hasNext() {
				return this.first.hasNext() ? true : this.second.hasNext();
			}

			@Override
			public T next() {
				if (this.first.hasNext()) {
					return this.first.next();
				} else if (this.second.hasNext()) {
					return this.second.next();
				}
				throw new NoSuchElementException("No more elements.");
			}
		}

		final class VPTIterator implements Iterator<T> {
			Stack<VPNode<T>> nodes = new Stack<>();

			VPTIterator(VPNode<T> root) {
				// Follow the nodes down and to the left, adding all of them to the stack.
				// This way we keep track of parents.
				while (root != null) {
					nodes.push(root);
					root = root.getInner();
				}
			}

			@Override
			public boolean hasNext() {
				return !nodes.isEmpty();
			}

			@Override
			public T next() {
				VPNode<T> node = null;
				try {
					node = nodes.pop();
				} catch (EmptyStackException e) {
					throw new NoSuchElementException();
				}

				// Take the data from the top of the stack and return it, then set up the next
				// top element of the stack.
				T result = node.getData();
				if (node.getOuter() != null) {
					node = node.getOuter();
					while (node != null) {
						nodes.push(node);
						node = node.getInner();
					}
				}
				return result;
			}
		}

		return new DoubleIterator(this.extras.iterator(), new VPTIterator(this.root));
	}
}
