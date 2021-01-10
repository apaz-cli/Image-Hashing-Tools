package app.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class VertexCover {

	// An approximate solution to the np-hard minimum vertex cover optimization
	// problem.
	public static int[] approxMinVertexCover(int[][] edges) {
		if (edges == null) throw new IllegalArgumentException();
		for (int[] edge : edges) if (edge.length != 2) throw new IllegalArgumentException();

		// Copy and sort the edges
		List<int[]> edgesRemaining = sortEdges(edges);

		// Calculate the cover
		List<Integer> bestCover = new ArrayList<>();
		while (!edgesRemaining.isEmpty()) {
			int[] currentEdge = edgesRemaining.remove(edgesRemaining.size() - 1);
			if (!bestCover.contains(currentEdge[0]) && !bestCover.contains(currentEdge[1])) {
				bestCover.add(currentEdge[0]);
				edgesRemaining = edgesRemaining.stream()
						.filter(edge -> edge[0] != currentEdge[0] && edge[1] != currentEdge[0])
						.collect(Collectors.toList());
			}
		}

		// Try to improve the cover by trying to remove each vertex in it, and seeing if
		// it's still a cover.
		newCover: for (Integer i : bestCover) {
			List<Integer> toCheck = new ArrayList<>(bestCover);
			toCheck.remove((Object) i);

			if (isCover(toCheck, edges)) {
				bestCover = toCheck;
				break newCover;
			}

		}

		return bestCover.stream().mapToInt(i -> i).toArray();
	}

	/********************/
	/* Helper Functions */
	/********************/

	private static List<int[]> sortEdges(int[][] edges) {
		// Sort within each edge
		int tmp;
		for (int[] edge : edges) {
			if (edge[0] > edge[1]) {
				tmp = edge[1];
				edge[1] = edge[0];
				edge[0] = tmp;
			}
		}

		// Sort the edge list by most used first
		Comparator<int[]> cmp = (first, second) -> {
			int firstTotal = 0;
			int secondTotal = 0;
			int e0, e1;
			int f0 = first[0], f1 = first[1];
			int s0 = second[0], s1 = second[1];
			for (int[] edge : edges) {
				e0 = edge[0];
				e1 = edge[1];
				if (e0 == f0 || e0 == f1) firstTotal += 1;
				if (e0 == s0 || e0 == s1) secondTotal += 1;
				if (e1 == f0 || e1 == f1) firstTotal += 1;
				if (e1 == s0 || e1 == s1) secondTotal += 1;
			}
			return firstTotal < secondTotal ? -1 : firstTotal == secondTotal ? 0 : 1;
		};

		// Return a sorted copy
		List<int[]> edgesRemaining = new ArrayList<>(Arrays.asList(edges));
		Collections.sort(edgesRemaining, cmp);
		return edgesRemaining;
	}

	private static boolean isCover(List<Integer> query, int[][] edges) {
		for (int[] edge : edges) if (!query.contains(edge[0]) && !query.contains(edge[1])) return false;
		return true;
	}

	public static void main(String args[]) {
		int[][] a = new int[][] { { 0, 1 }, { 0, 2 }, { 1, 3 }, { 3, 4 }, { 1, 5 }, { 4, 5 }, { 5, 6 } };
		System.out.println(Arrays.toString(approxMinVertexCover(a)));

	}
}
