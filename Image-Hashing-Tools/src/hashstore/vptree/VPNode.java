package hashstore.vptree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import utils.Pair;

// This class is not to be used outside of the VPTree code. No unnecessary robustness checks.
class VPNode<T extends MetricComparable<? extends T>> implements VantagePoint<T>, Serializable {

	private static final long serialVersionUID = -7411873114167266972L;

	private static Random r = new Random();

	// Class is not public, so these are not actually visible
	T data;
	VantagePoint<T> innerChild;
	VantagePoint<T> outerChild;
	double radius;

	VPNode() {}

	VPNode(T datapoint, double radius, VantagePoint<T> innerChild, VantagePoint<T> outerChild) {
		this.data = datapoint;
		this.radius = radius;
		this.innerChild = innerChild;
		this.outerChild = outerChild;
	}

	T chooseVantagePoint(List<T> workingPartition) {
		// Chooses at random. This requires more research.
		return workingPartition.remove(r.nextInt(workingPartition.size()));
	}

	@SuppressWarnings("unchecked")
	double findMedianDistance(T vantage, List<T> workingPartition) {
		double[] distances = new double[workingPartition.size()];
		for (int i = 0; i < workingPartition.size(); i++) {
			distances[i] = ((MetricComparable<T>) vantage).distance(workingPartition.get(i));
		}
		Arrays.sort(distances);
		return distances.length % 2 == 0 ? distances[distances.length / 2]
				: (distances[distances.length / 2] + distances[(distances.length / 2) + 1]) / 2;

	}

	@SuppressWarnings("unchecked")
	Pair<List<T>, List<T>> partition(List<T> workingPartition, boolean parallel) {
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
		return new Pair<>(innerChildPartition, outerChildPartition);
	}

	@SuppressWarnings("unchecked")
	Pair<List<T>, List<T>> setDataRadiusGetChildren(List<T> workingPartition) {
		this.data = this.chooseVantagePoint(workingPartition); // Removes data point

		List<Pair<T, Double>> distances = new ArrayList<>();
		for (int i = 0; i < workingPartition.size(); i++) {
			T item = workingPartition.get(i);
			distances.add(new Pair<T, Double>(item, ((MetricComparable<T>) this.data).distance(item)));
		}

		Collections.sort(distances, (first, second) -> Double.compare(first.getValue(), second.getValue()));

		System.out.println(distances.size());
		System.out.println(distances.size() % 2 == 1);
		System.out.println(distances.get((distances.size() / 2) + 1).getValue());

		// @nof
		// radius is the median, which is the middle or Average of two middle
		this.radius = distances.size() % 2 == 1 ? 
				distances.get((distances.size() / 2) + 1).getValue() : 
			   (distances.get( distances.size() / 2).getValue() + distances.get((distances.size() / 2) + 1).getValue() ) / 2; 
		// @dof

		int middle = (distances.size() / 2) + 1;
		// Scan left and find the index of the rightmost element of the inner partition
		// from the middle.
		int indexOfRightmostInner = middle;
		System.out.println(distances.size());
		System.out.println((distances.size() / 2) + 1);
		System.out.println();
		System.out.println(radius);
		System.out.println(distances.get(indexOfRightmostInner).getValue());
		while (radius <= distances.get(indexOfRightmostInner--).getValue() && indexOfRightmostInner != 0) {
			// System.out.println(indexOfRightmostInner);
		}

		// The middle element belonged to the inner partition, so the cutoff may still
		// be to the right.
		// Scan until we hit something from the outer partition.
		if (indexOfRightmostInner == middle) {
			while (true) {
				if (distances.get(indexOfRightmostInner).getValue() < radius) indexOfRightmostInner++;
				else {
					indexOfRightmostInner--; // We overshot it, so backtrack one
					break;
				}
			}
		}

		// Sublist is bound-exclusive, so add one.
		int splitIndex = indexOfRightmostInner + 1;

		List<T> innerPartition = distances.subList(0, splitIndex).stream().map(p -> p.getKey())
				.collect(Collectors.toList());
		List<T> outerPartition = distances.subList(splitIndex, distances.size()).stream().map(p -> p.getKey())
				.collect(Collectors.toList());

		return new Pair<>(innerPartition, outerPartition);
	}

	List<T> toList() throws FileNotFoundException, ClassNotFoundException, IOException {
		List<T> children = new ArrayList<T>();
		Stack<VantagePoint<T>> stk = new Stack<>();
		stk.push(this);

		while (!stk.empty()) {
			VantagePoint<T> popped = stk.pop();
			if (popped instanceof VPNode) {
				VPNode<T> vpn = (VPNode<T>) popped;
				stk.push(vpn.outerChild);
				stk.push(vpn.innerChild);
			} else if (popped instanceof VPReference) {
				stk.push(((VPReference<T>) popped).load().getRoot());
			} else { // VPLeaf
				children.addAll((VPLeaf<T>) popped);
			}
		}

		return children;
	}

	Iterator<T> iteratorOfChildren() {
		class VPIterator implements Iterator<T> {

			Stack<Object> stk = new Stack<>();

			public VPIterator(VPNode<T> vpNode) {
				stk.push(vpNode);
			}

			@Override
			public boolean hasNext() {
				return !stk.empty();
			}

			@Override
			@SuppressWarnings("unchecked")
			public T next() {
				// Pop an item from a nonempty stack. If it empty, handle accordingly.
				Object popped = null;
				try {
					popped = stk.pop();
				} catch (EmptyStackException e) {
					throw new NoSuchElementException();
				}

				// If we have an element ready to go, just spit it out.
				if (popped instanceof MetricComparable<?>) return (T) popped;

				// If we don't, then we must further build the stack. Any stack that is nonempty
				// has at least one item still in it.
				// Traverse to the left until you find a leaf, building the stack as you go.
				while (true) {
					if (popped instanceof VPNode) {
						VPNode<T> p = (VPNode<T>) popped;
						stk.push(p.outerChild);
						popped = p.innerChild;
					} else if (popped instanceof VPReference) {
						VPTree<?> subtree;
						try {
							subtree = VPTree.load((VPReference<T>) popped);
						} catch (ClassNotFoundException | IOException e) {
							e.printStackTrace();
							return null;
						}
						popped = subtree.getRoot();
					} else { // popped instanceof VPLeaf
						for (T item : (VPLeaf<T>) popped) {
							stk.push(item);
						}
						return (T) stk.pop();
					}
				}

			}

		}
		return new VPIterator(this);
	}

}
