package vptree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class VPTTest {

	public static double listDist(List<Float> l1, List<Float> l2) {
		if (l1.size() != l2.size()) { return Double.MAX_VALUE; }

		float f1, f2, diff;
		double sumOfSquares = 0;
		for (int i = 0; i < l1.size(); i++) {
			f1 = l1.get(i);
			f2 = l2.get(i);
			diff = (f1 - f2);
			sumOfSquares += diff * diff;
		}

		return Math.sqrt(sumOfSquares);
	}

	@Test
	void testTree() {
		int vectorDimension = 6;
		int numPoints = 2000000;

		Random r = new Random();
		List<List<Float>> datapoints = new ArrayList<>();
		for (int i = 0; i < numPoints; i++) {
			ArrayList<Float> l = new ArrayList<>();
			for (int j = 0; j < vectorDimension; j++) {
				l.add(r.nextFloat() * 50);
			}
			l.trimToSize();
			datapoints.add(l);
		}
		System.out.println(numPoints + " datapoints generated. Building tree.");
		
		VPTree<List<Float>> tree = new VPTree<List<Float>>(datapoints, VPTTest::listDist);
		System.out.println("Finished building tree.");

		// Assert size works. This is a native callback, so it's legitimately worth testing.
		assert (tree.size() == numPoints);

		// Build a query point and try out nn and knn
		List<Float> query = new ArrayList<>();
		for (int j = 0; j < vectorDimension; j++) {
			query.add(r.nextFloat() * 50);
		}

		VPEntry<List<Float>> nn = tree.nn(query);
		double dist = listDist(query, nn.item);
		System.out.println(nn);
		System.out.println(new VPEntry<>(nn.item, dist));

		for (VPEntry<List<Float>> entry : tree.knn(query, 100)) {
			System.out.println(entry);
		}
		
		// Close the tree when finished.
		assert (tree.isOpen());
		tree.close();
		assert (!tree.isOpen());
	}

	public static void main(String[] args) {
		new VPTTest().testTree();
	}
}
