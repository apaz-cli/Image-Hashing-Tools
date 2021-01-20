
package app.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import utils.Pair;

public abstract class VertexCover {
	private VertexCover() {
	}

	// An approximate solution to the np-hard minimum vertex cover optimization
	// problem.
	public static int[] approxMinVertexCover(int[][] edges) {
		if (edges == null) throw new IllegalArgumentException();
		for (int[] edge : edges) if (edge.length != 2) throw new IllegalArgumentException();
		if (edges.length == 0) return new int[0];
		if (edges.length == 1) return new int[] { edges[0][0] };

		// Copy and sort the edges
		List<int[]> sortedEdges = sortEdges(edges);
		List<int[]> sortedEdgesReordered = new ArrayList<>(sortedEdges);
		Collections.swap(sortedEdgesReordered, 0, 1); // Is large enough due to previous checks

		// Calculate two covers and find the best one
		List<List<Integer>> trialCovers = new ArrayList<>();
		trialCovers.add(badCover(sortedEdges));
		trialCovers.add(badCover(sortedEdgesReordered));
		List<Integer> bestCover = trialCovers.stream()
				.sorted((l1, l2) -> l1.size() < l2.size() ? -1 : l1.size() == l2.size() ? 0 : 1).findFirst().get();

		return findBestCover(trialCovers, bestCover, edges).stream().mapToInt(i -> i).toArray();
	}

	public static <T> List<T> approxMinVertexCover(List<T> vertices, List<Pair<T, T>> edges) {
		if (vertices == null) throw new NullPointerException();
		if (edges == null) throw new NullPointerException();
		if (edges.size() == 0) return new ArrayList<T>();

		// Remove duplicates
		vertices = new ArrayList<>(new HashSet<>(vertices));

		// Ensure that all edges are present in the vertex list, and that none are null.
		boolean unlisted = vertices.size() == 0;
		for (Pair<T, T> p : edges) {
			if (p == null) throw new NullPointerException();
			if (p.getKey() == null || p.getValue() == null) throw new NullPointerException();
			if (!vertices.contains(p.getKey()) || !vertices.contains(p.getValue())) {
				unlisted = true;
				break;
			}
		}
		if (unlisted) throw new IllegalArgumentException(
				"Cannot find a vertex cover of an empty graph for vertices that are not in the graph.");

		// Zip with indices in map
		HashMap<T, Integer> bimap = new HashMap<>();
		for (int i = 0; i < vertices.size(); i++) {
			bimap.put(vertices.get(i), i);
		}

		// We intend to call the other approxMinVertexCover() method, translating the
		// results to be the results for this one.

		// Pack the edges into the expected format
		int[][] translatedEdges = new int[edges.size()][2];
		for (int i = 0; i < edges.size(); i++) {
			Pair<T, T> edge = edges.get(i);
			translatedEdges[i][0] = bimap.get(edge.getKey());
			translatedEdges[i][1] = bimap.get(edge.getValue());
		}

		int[] results = VertexCover.approxMinVertexCover(translatedEdges);

		final List<T> verts = vertices;
		return IntStream.of(results).mapToObj(i -> verts.get(i)).filter(o -> o != null).distinct()
				.collect(Collectors.toList());
	}

	/********************/
	/* Helper Functions */
	/********************/

	private static List<Integer> badCover(List<int[]> edges) {
		List<Integer> cover = new ArrayList<>();
		while (!edges.isEmpty()) {
			int[] currentEdge = edges.remove(edges.size() - 1);
			if (!cover.contains(currentEdge[0]) && !cover.contains(currentEdge[1])) {
				cover.add(currentEdge[0]);
				edges = edges.stream().filter(edge -> edge[0] != currentEdge[0] && edge[1] != currentEdge[0])
						.collect(Collectors.toList());
			}
		}
		return cover;
	}

	private static List<Integer> findBestCover(List<List<Integer>> trialCovers, List<Integer> bestCover,
			int[][] edges) {
		// Try to improve the cover by trying to remove each vertex in it, and seeing if
		// it's still a cover. Then repeat the process and keep the best one.
		Stack<List<Integer>> betterCovers = new Stack<>();
		betterCovers.push(new ArrayList<>(bestCover));
		while (!betterCovers.empty()) {
			List<Integer> check = betterCovers.pop();
			for (Integer i : check) {
				List<Integer> toCheck = new ArrayList<>(check);
				toCheck.remove(i);
				if (isCover(toCheck, edges)) {
					if (toCheck.size() < bestCover.size()) bestCover = toCheck;
					betterCovers.push(toCheck);
				}

			}
		}
		return bestCover;
	}

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

		// Sort and return a copy
		List<int[]> sortedEdges = new ArrayList<>(Arrays.asList(edges));
		Collections.sort(sortedEdges, cmp);
		return sortedEdges;
	}

	private static boolean isCover(List<Integer> query, int[][] edges) {
		if (query.size() == 0) return false;
		for (int[] edge : edges) if (!(query.contains(edge[0]) || query.contains(edge[1]))) return false;
		return true;
	}

	public static void main(String args[]) {
		int[][] a = new int[][] { { 4, 5 }, { 1, 3 }, { 0, 1 }, { 0, 2 }, { 3, 4 }, { 1, 5 }, { 5, 6 } };
		System.out.println(Arrays.toString(approxMinVertexCover(a)));

		Random r = new Random();
		int numVerteces = 15;
		int[][] b = new int[numVerteces][2];
		for (int i = 0; i < numVerteces; i++) {
			b[i][0] = r.nextInt(numVerteces);
			b[i][1] = b[i][0];
			while ((b[i][1] = r.nextInt(numVerteces)) == b[i][0]);
		}
		System.out.println(Arrays.deepToString(b).replace('[', '{').replace(']', '}'));
		System.out.print(Arrays.toString(approxMinVertexCover(b)));
	}
}
