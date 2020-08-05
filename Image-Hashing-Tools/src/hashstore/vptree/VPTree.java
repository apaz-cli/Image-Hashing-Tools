package hashstore.vptree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import image.PixelUtils;
import utils.NNList;
import utils.Pair;

public class VPTree<T extends MetricComparable<? extends T>> extends AbstractCollection<T> implements Serializable {

	private static final long serialVersionUID = -4807284349425268252L;
	private static final int maxHeight = 10;
	private static final int leafCapacity = 100;

	private VantagePoint<T> root;
	private int size;
	private VPReference<T> serialized;

	public VPTree() {
		this(new ArrayList<>());
	}

	public VPTree(Collection<T> datapoints) {
		this(datapoints, new File(System.getProperty("user.dir") + File.separator + "Vantage Point Trees"));
	}

	public VPTree(File containingDirectory) {
		this(new ArrayList<>(), containingDirectory);
	}

	public VPTree(Collection<T> datapoints, File containingDirectory) {
		PixelUtils.assertNotNull("datapoints", datapoints);
		VPLeaf<T> duplicateTrimmed = new VPLeaf<>(datapoints);
		this.root = buildTree(duplicateTrimmed);
		this.size = duplicateTrimmed.size();
		this.serialized = chooseFile(containingDirectory);
		try {
			this.serialized.save(this);
		} catch (IOException e) {
			System.err.println("Error saving the constructed tree to disk: ");
			e.printStackTrace();
		}
	}

	// ****************** //
	// * Helper Methods * //
	// ****************** //

	private VantagePoint<T> buildTree(List<T> workingPartition) {
		if (workingPartition.size() < leafCapacity) return new VPLeaf<T>(workingPartition);

		VantagePoint<T> rootToReturn = null;

		// Whenever a part of the tree still needs to be created, push it onto the
		// stack.
		@SuppressWarnings("serial")
		class SubtreeCreationInformation extends ArrayList<T> {
			VPNode<T> parent;

			SubtreeCreationInformation(VPNode<T> parent, List<T> datapoints) {
				super(datapoints);
				this.parent = parent;
			}
		}
		Stack<SubtreeCreationInformation> subtreesToCreateOnLeft = new Stack<>(),
				subtreesToCreateOnRight = new Stack<>();
		{
			@SuppressWarnings("serial")
			class NodeCreationInformation extends ArrayList<T> {
				// Extend to have to keep track of one less object per subtree and save a bunch
				// of memory
				VPNode<T> parent;
				int depthOfDatapoints;

				NodeCreationInformation(VPNode<T> parent, List<T> datapoints, int depthOfDatapoints) {
					super(datapoints);
					this.parent = parent;
					this.depthOfDatapoints = depthOfDatapoints;
				}
			}
			Stack<NodeCreationInformation> toBuildOnLeft = new Stack<>(), toBuildOnRight = new Stack<>();

			{ // Build root. Since we didn't return at the beginning, we know we have to build
				// a node.
				VPNode<T> c;
				rootToReturn = c = new VPNode<T>();
				Pair<List<T>, List<T>> partitions = c.setDataRadiusGetChildren(workingPartition);
				toBuildOnLeft.push(new NodeCreationInformation(c, partitions.getKey(), 1));
				toBuildOnRight.push(new NodeCreationInformation(c, partitions.getValue(), 1));
			}

			// Make the tree. Traversal order doesn't matter, we're keeping track of all
			// these things anyway.
			while (!toBuildOnLeft.empty()) {
				while (!toBuildOnRight.empty()) {
					// Determine type of node to make at this location, and either save the
					// information necessary to construct the subtree, or build it and place it in,
					// keeping track of the work that still needs to be done.

					System.out.println("Stack Sizes: " + toBuildOnLeft.size() + " " + toBuildOnRight.size());
					// The code here is repeated below for the left.
					NodeCreationInformation rightInfo = toBuildOnRight.pop();
					if (rightInfo.depthOfDatapoints >= maxHeight) {
						// Purge the now-unnecessary depth information by copying the list
						subtreesToCreateOnRight
								.add(new SubtreeCreationInformation(rightInfo.parent, new ArrayList<>(rightInfo)));
					} else if (rightInfo.size() >= leafCapacity) {
						VPNode<T> newNode = new VPNode<>();
						Pair<List<T>, List<T>> innerOuterChildren = newNode.setDataRadiusGetChildren(rightInfo);
						rightInfo.parent.outerChild = newNode;
						toBuildOnLeft.push(new NodeCreationInformation(newNode, innerOuterChildren.getKey(),
								rightInfo.depthOfDatapoints + 1));
						toBuildOnRight.push(new NodeCreationInformation(newNode, innerOuterChildren.getValue(),
								rightInfo.depthOfDatapoints + 1));
					} else {
						rightInfo.parent.outerChild = new VPLeaf<>(rightInfo);
					}
				}

				// Now do exactly the same as above for the left. Consume both stacks until
				// they're empty.
				NodeCreationInformation leftInfo = toBuildOnLeft.pop();
				if (leftInfo.depthOfDatapoints >= maxHeight) {
					subtreesToCreateOnLeft
							.add(new SubtreeCreationInformation(leftInfo.parent, new ArrayList<>(leftInfo)));
				} else if (leftInfo.size() >= leafCapacity) { // If we shouldn't make a leaf yet, make a node
					VPNode<T> newNode = new VPNode<>();
					Pair<List<T>, List<T>> innerOuterChildren = newNode.setDataRadiusGetChildren(leftInfo);
					leftInfo.parent.innerChild = newNode;
					toBuildOnLeft.push(new NodeCreationInformation(newNode, innerOuterChildren.getKey(),
							leftInfo.depthOfDatapoints + 1));
					toBuildOnRight.push(new NodeCreationInformation(newNode, innerOuterChildren.getValue(),
							leftInfo.depthOfDatapoints + 1));
				} else { // Leaf
					leftInfo.parent.innerChild = new VPLeaf<>(leftInfo);
				}
			}
		}

		// Build each subtree, and place it into the tree. Theoretically this could be
		// multithreaded, as could building the tree above, and there could be
		// significant gains in speed, but memory consumption is a problem.
		while (!subtreesToCreateOnLeft.empty()) {
			SubtreeCreationInformation subInfo = subtreesToCreateOnLeft.pop();
			VPTree<T> vpt = new VPTree<>(subInfo);
			subInfo.parent.innerChild = vpt.serialized;
		}
		while (!subtreesToCreateOnRight.empty()) {
			SubtreeCreationInformation subInfo = subtreesToCreateOnRight.pop();
			VPTree<T> vpt = new VPTree<>(subInfo);
			subInfo.parent.outerChild = vpt.serialized;
		}

		return rootToReturn;
	}

	private VPReference<T> chooseFile(File containingDirectory) {
		PixelUtils.assertNotNull("containingDirectory", containingDirectory);

		containingDirectory.mkdirs();
		if (!containingDirectory.isDirectory()) throw new IllegalArgumentException(
				"The directory specified does not exist or is not a directory. Got: " + containingDirectory);

		VPReference<T> target = new VPReference<>(containingDirectory.getPath() + File.separator + this.hashCode());
		int offset = 1;
		while (target.exists()) {
			target = new VPReference<>(containingDirectory.getPath() + File.separator + (this.hashCode() + offset++));
		}

		return target;
	}

	// *************************** //
	// * VPTree-Specific Methods * //
	// *************************** //

	public List<T> toList() throws IOException {
		try {
			if (this.root instanceof VPLeaf<?>) return new ArrayList<>((VPLeaf<T>) this.root);
			else return this.root instanceof VPNode ? ((VPNode<T>) this.root).toList()
					: ((VPReference<T>) this.root).load().toList();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	// TODO dump whole tree to text file
	public VPTree<T> rebuild() throws IOException {
		return new VPTree<T>(this.toList());
	}

	public T NN(T query) throws IOException {
		List<T> l = this.kNN(query, 1);
		return l.isEmpty() ? null : l.get(0);
	}

	public List<T> kNN(T query, int k) throws IOException {
		if (query == null) throw new NullPointerException("Query is null.");
		@SuppressWarnings("unchecked")
		final MetricComparable<T> target = (MetricComparable<T>) query;
		if (k <= 0) throw new IllegalArgumentException("k cannot be negative or zero. Got: " + k);
		if (k > this.size) return this.toList().stream()
				.sorted((l, r) -> Double.compare(target.distance(l), target.distance(r))).collect(Collectors.toList());

		// This list is sorted backwards, such that the farthest node from the query
		// point is first.
		NNList<Pair<T, Double>> NNs = new NNList<Pair<T, Double>>(k,
				(first, second) -> { return Double.compare(first.getValue(), second.getValue()); });

		double tau = 0; // The distance from the farthest nearest neighbor to the query point. We keep
						// track of this because we don't want to traverse sections of the tree that we
						// don't need to.

		// Keep track of the nodes which we need to visit.
		final Stack<VantagePoint<T>> toTraverse = new Stack<>();
		toTraverse.push(this.root);

		// Traverse the tree for the neighbors
		VantagePoint<T> currentVantagePoint = null;
		while (!toTraverse.empty()) {
			currentVantagePoint = toTraverse.pop();
			// Until we hit a leaf in all avenues we're exploring, keep traversing.
			if (currentVantagePoint instanceof VPNode<?>) {
				VPNode<T> currentNode = (VPNode<T>) currentVantagePoint;
				final double radius = currentNode.radius;
				final double distCurrentToQuery = target.distance(currentNode.data);

				// Update tau, the distance to the farthest nearest neighbor away, and add it as
				// a NN if applicable. We don't have to search parts of the tree containing only
				// points guaranteed to be worse than the ones we've already found.
				NNs.add(new Pair<T, Double>(currentNode.data, distCurrentToQuery));

				tau = NNs.getWorstNN().getValue();
				System.out.println("Distance: " + distCurrentToQuery);
				System.out.println("Tau: " + tau);

				// Determine where to traverse inner, outer, or both
				if (distCurrentToQuery < radius - tau) { // No overlap, entirely inside
					System.out.println("Traversing Left");
					toTraverse.push(currentNode.innerChild);
				} else {
					if (distCurrentToQuery > radius + tau) { // No overlap, entirely outside
						System.out.println("Traversing Right");
						toTraverse.push(currentNode.outerChild);
					} else { // The hyperspheres do overlap, so search both
						System.out.println("Traversing Left and Right");
						toTraverse.push(currentNode.innerChild);
						toTraverse.push(currentNode.outerChild);
					}
				}
			} else if (currentVantagePoint instanceof VPReference) {
				// If it needs to be done, load the subtree into memory and continue from its
				// root.
				System.out.println("Loading a subtre.");
				toTraverse.push(this.loadSubtree((VPReference<T>) currentVantagePoint));
			}

			// Once we hit a leaf, add the whole list of data and if the stack is empty then
			// proceed on
			else {
				VPLeaf<T> currentLeaf = (VPLeaf<T>) currentVantagePoint;
				NNs.addAll(currentLeaf.stream().map((t) -> { return new Pair<T, Double>(t, target.distance(t)); })
						.collect(Collectors.toList()));
				tau = NNs.getWorstNN().getValue();
				System.out.println("At leaf, Tau: " + tau);
			}
		}

		// Place into a list, sort (because we must end with a leaf which is inserted
		// unsorted)
		NNs.justify();
		List<Pair<T, Double>> NNsList = new ArrayList<>(NNs); // Sort smallest first
		return NNsList.stream().map(p -> p.getKey()).collect(Collectors.toList());
	}

	public List<T> allWithinDistance(T datapoint, double distance) {
		// TODO write
		return null;
	}

	// ********************** //
	// * Collection Methods * //
	// ********************** //

	// Duplicates not permitted
	@Override
	public synchronized boolean add(T datapoint) {
		if (datapoint == null) return false;

		if (this.root instanceof VPLeaf) {
			VPLeaf<T> r = (VPLeaf<T>) this.root;
			r.add(datapoint);
			if (r.size() > leafCapacity) this.root = bifurcateLeaf(r);
			this.size++;
			return true;
		}

		// If it's not the root, then the root is a VPNode and we must traverse the
		// tree.
		@SuppressWarnings("unchecked")
		MetricComparable<T> target = (MetricComparable<T>) datapoint;

		int depth = 0;
		VPNode<T> parent = null;
		VantagePoint<T> currentVantagePoint = this.root;
		VPTree<T> currentTree = this;
		List<VPReference<T>> visitedTreesForSizeUpdate = new ArrayList<>();
		visitedTreesForSizeUpdate.add(this.serialized);

		// Traverse to the leaf that is the location for this datapoint.
		while (true) {
			if (currentVantagePoint instanceof VPNode) {
				// Traverse toward where this datapoint needs to go
				// @nof
				VPNode<T> currentNode = (VPNode<T>) currentVantagePoint;
				currentVantagePoint = target.within(currentNode.radius, currentNode.data) ? 
					currentNode.innerChild: 
					currentNode.outerChild;
				// @dof
				parent = currentNode;
				depth++;
			} else if (currentVantagePoint instanceof VPReference) {
				try {
					currentTree = ((VPReference<T>) currentVantagePoint).load();
					visitedTreesForSizeUpdate.add(currentTree.serialized);
					depth = 0;
					parent = null;
					currentVantagePoint = currentTree.root;
				} catch (ClassNotFoundException | IOException e) {
					System.err.println("There was an error loading a subtree.");
					e.printStackTrace();
					return false;
				}
			} else { // Leaf
				break;
			}
		}

		// Add it to the leaf, and then decide what to replace the leaf with.
		VPLeaf<T> currentLeaf = (VPLeaf<T>) currentVantagePoint;
		boolean isOnLeft = currentLeaf == parent.innerChild;
		currentLeaf.add(datapoint);

		// If we must, then create a new subtree out of the leaf we're in, then update
		// where the leaf currently is with a reference.
		if (depth >= maxHeight) {
			// Constructor saves the resulting tree to disk already
			VPTree<T> vpt = new VPTree<T>(currentLeaf, this.serialized.getParentFile());
			if (isOnLeft) parent.innerChild = vpt.serialized;
			else parent.outerChild = vpt.serialized;
			// It already knows what size it's supposed to be, so don't add it to the list
			// of trees.
		}
		// Otherwise, add it the leaf and branch it if necessary.
		else if (currentLeaf.size() > leafCapacity) {
			VPNode<T> bifurcated = bifurcateLeaf(currentLeaf);
			if (isOnLeft) parent.innerChild = bifurcated;
			else parent.outerChild = bifurcated;
		}
		// Else do nothing, because no further edits to the structure need to be made
		// and the leaf is already inserted.

		// Update all the sizes for the trees we visited
		for (VPReference<T> ref : visitedTreesForSizeUpdate) {
			try {
				VPTree<T> t = ref.load();
				t.size++;
				ref.save(t);
			} catch (ClassNotFoundException | IOException e) {
				System.err.println("There was an issue updating the size of one of the trees.");
				e.printStackTrace();
			}
		}
		return true;
	}

	private VPNode<T> bifurcateLeaf(VPLeaf<T> leaf) {
		VPNode<T> node = new VPNode<T>();
		Pair<List<T>, List<T>> innerOuter = node.setDataRadiusGetChildren(leaf);
		node.innerChild = new VPLeaf<>(innerOuter.getKey());
		node.outerChild = new VPLeaf<>(innerOuter.getValue());
		return node;
	}

	@Override
	public boolean contains(Object o) {
		@SuppressWarnings("unchecked") // Throw ClassCastException if impossible conversion.
		MetricComparable<T> target = (MetricComparable<T>) o;

		// Traverse the tree until you find a leaf
		VantagePoint<T> currentVantagePoint = this.root;
		while (true) { // Root will never be null
			if (currentVantagePoint instanceof VPNode<?>) {
				VPNode<T> current = (VPNode<T>) currentVantagePoint;
				T data = current.data;

				if (data.equals(o)) return true;

				if (target.within(current.radius, data)) {
					currentVantagePoint = current.innerChild;
				} else {
					currentVantagePoint = current.outerChild;
				}
			} else if (currentVantagePoint instanceof VPReference) {
				try {
					currentVantagePoint = this.loadSubtree((VPReference<T>) currentVantagePoint);
				} catch (IOException e) {
					// We can't provide a conclusive answer because we can't visit the subtree, so
					// let's just make it explode with a null pointer exception instead.
					throw new IllegalStateException(e);
				}
			} else { // is leaf
				return ((VPLeaf<?>) currentVantagePoint).contains(o);
			}
		}
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public synchronized boolean remove(Object o) {

		// Throw ClassCastException if impossible conversion.
		MetricComparable<T> target = (MetricComparable<T>) o;
		VPTree<T> currentSubtree = this;
		List<VPReference<T>> toUpdateSize = new ArrayList<>(); // Every vpt that has been visited
		toUpdateSize.add(this.serialized);

		VantagePoint<T> currentVantagePoint = this.root;
		VPNode<T> lastVantagePoint = null; // Null iff current is root

		while (true) { // Root will never be null
			if (currentVantagePoint instanceof VPNode<?>) {
				VPNode<T> current = (VPNode<T>) currentVantagePoint;
				T data = current.data;

				// If this node contains the data, remove it.
				if (data.equals(o)) {
					if (lastVantagePoint.innerChild == current) lastVantagePoint.innerChild = current.innerChild;
					else lastVantagePoint.outerChild = current.innerChild;

					// TODO replace with addAll when relevant
					List<T> toInsert = null;
					if (current.outerChild instanceof VPNode) {
						try {
							toInsert = current.toList();
						} catch (ClassNotFoundException | IOException e) {
							System.out.print(
									"Something went wrong rewriting part of the tree, and it is now likely corrupt.");
							e.printStackTrace();
							return true;
						}
					} else if (current.outerChild instanceof VPReference) {
						List<T> subtreeList = null;
						try {
							subtreeList = ((VPReference<T>) current.outerChild).load().toList();
						} catch (ClassNotFoundException | IOException e) {
							System.out.print(
									"Something went wrong rewriting part of the tree, and it is now likely corrupt.");
							e.printStackTrace();
							return true;
						}
						this.addAll(subtreeList);
					} else { // leaf
						this.addAll((VPLeaf<T>) current.outerChild);
					}

					this.addAll(toInsert);

					return true;
				}

				// If we've gotten here, this is not the node we're looking for, so keep
				// looking.
				lastVantagePoint = current;
				if (target.within(current.radius, data)) {
					currentVantagePoint = current.innerChild;
				} else {
					currentVantagePoint = current.outerChild;
				}
			} else if (currentVantagePoint instanceof VPReference) {
				// Load the tree and continue traversal there, keeping track of the relevant
				// information
				try {
					VPTree<T> loadedTree = ((VPReference<T>) currentVantagePoint).load();
					lastVantagePoint = null;
					currentVantagePoint = loadedTree.root;
					currentSubtree = loadedTree;
				} catch (IOException | ClassNotFoundException e) {
					// If loading goes wrong, notify and exit.
					System.err.println("There was an error traversing a subtree down.");
					e.printStackTrace();
					return false;
				}
			} else { // is leaf
				// Remove
				boolean retValue = ((VPLeaf<T>) currentVantagePoint).remove(o);

				// Write changes
				toUpdateSize.remove(currentSubtree.serialized);
				toUpdateSize.remove(this.serialized);
				for (VPReference<T> ref : toUpdateSize) {
					try {
						VPTree<T> vpt;
						vpt = ref.load();
						vpt.size--;
						ref.save(vpt);
					} catch (IOException | ClassNotFoundException e) {
						System.err.println(
								"Something went wrong updating the size of a subtree. Found the object, and removed it, but size is inaccurate for some number of subtrees, which may or may not still be present on disk.");
						e.printStackTrace();
						return retValue;
					}

				}

				try {
					this.size--;
					if (this != currentSubtree) {
						currentSubtree.size--;
						currentSubtree.serialized.save(currentSubtree);
					}
				} catch (IOException e) {
					System.err.println("Something went wrong saving changes to a subtree.");
					e.printStackTrace();
					return retValue;
				}
				return retValue;
			}
		}
	}

	@Override
	public synchronized int size() {
		return this.size;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> c) {
		boolean changed = false;
		for (T item : c) if (this.add(item)) changed = true;
		return changed;
	}

	@Override
	public synchronized Iterator<T> iterator() {
		if (this.root instanceof VPNode) return ((VPNode<T>) this.root).iteratorOfChildren();
		Iterator<T> ret = null;
		try {
			ret = this.root instanceof VPLeaf ? ((VPLeaf<T>) this.root).iterator()
					: ((VPReference<T>) this.root).load().iterator();
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("The root of this tree is a reference, and it could not be loaded.");
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public void clear() {
		// Clean up subtrees of this one
		Stack<VantagePoint<T>> stk = new Stack<>();
		stk.push(this.root);
		while (!stk.empty()) {
			VantagePoint<T> popped = stk.pop();
			if (popped instanceof VPNode) {
				VPNode<T> vpn = (VPNode<T>) popped;
				stk.push(vpn.outerChild);
				stk.push(vpn.innerChild);
			} else if (popped instanceof VPReference) {
				try {
					((VPReference<T>) popped).load().clear();
				} catch (ClassNotFoundException | IOException e) {
					System.err.println("There was an error loading a subtree to delete it, it may still be on disk.");
					e.printStackTrace();
				}
			} // Do nothing at VPLeaves, they will fall off the object graph
		}

		// Clean up this tree
		this.size = 0;
		this.root = new VPLeaf<>();
		this.serialized.delete();
	}

	// ***************** //
	// * Serialization * //
	// ***************** //

	public File getSerializedLocation() { return this.serialized; }

	void setSerialized(VPReference<T> newSer) { this.serialized = newSer; }

	VantagePoint<T> getRoot() { return this.root; }

	public File save() throws FileNotFoundException, IOException {
		this.serialized.save(this);
		return this.serialized;
	}

	static VPTree<?> load(File from) throws FileNotFoundException, IOException, ClassNotFoundException {
		VPReference<?> ref = new VPReference<>(from.getPath());
		return ref.load();
	}

	private VantagePoint<T> loadSubtree(VPReference<T> subtree) throws FileNotFoundException, IOException {
		try {
			return subtree.load().root;
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public void clearOnExit() {
		Thread exitTask = new Thread(new Runnable() {
			@Override
			public void run() {
				clear();
			}
		});
		Runtime.getRuntime().addShutdownHook(exitTask);
	}

	public void debugPrintTraversal() {
		// start from level 1 -- till height of the tree
		int level = 1;

		// run till printLevel() returns false
		while (printLevel(root, level))
			level++;
	}

	private boolean printLevel(VantagePoint<T> current, int level) {
		if (current == null) {
			System.out.print("NULL ");
			return false;
		}

		if (current instanceof VPLeaf) {
			System.out.print("LEAF ");
			return false;
		}

		if (current instanceof VPReference) {
			try {
				printLevel(loadSubtree((VPReference<T>) current), level);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		VPNode<T> c = (VPNode<T>) current;
		if (level == 1) {
			System.out.print("NODE ");
			return true;
		}

		boolean left = printLevel(c.innerChild, level - 1);
		boolean right = printLevel(c.outerChild, level - 1);

		return left || right;
	}
}
