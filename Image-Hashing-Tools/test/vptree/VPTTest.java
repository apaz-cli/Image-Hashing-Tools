package vptree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class VPTTest {

	public static double listDist(List<Float> l1, List<Float> l2) {
		if (l1.size() != l2.size()) { return Double.MAX_VALUE; }

		float f1, f2;
		double sumOfSquares = 0;
		for (int i = 0; i < l1.size(); i++) {
			f1 = l1.get(i);
			f2 = l2.get(i);
			sumOfSquares += f1 * f1 + f2 * f2;
		}

		return Math.sqrt(sumOfSquares);
	}

	@Test
	void testTree() {
		int vectorDimension = 6;
		int numPoints = 20000000;

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

		System.out.println("Datapoints generated. Building tree.");
		VPTree<List<Float>> tree = new VPTree<List<Float>>(datapoints, VPTTest::listDist);
		System.out.println("Done building tree.");
		
		assert(tree.size() == numPoints);
		
		List<Float> query = new ArrayList<>();
		for (int j = 0; j < vectorDimension; j++) {
			query.add(r.nextFloat() * 50);
		}

		for (VPEntry<List<Float>> entry : tree.knn(query, 100)) {
			System.out.println(entry);
		}

		System.out.println(tree.nn(query));

		tree.close();
	}

	public static void main(String[] args) {
		new VPTTest().testTree();
	}
}
