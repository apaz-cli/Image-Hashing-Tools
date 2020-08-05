package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class BenchmarkRunner {

	// ******************** //
	// * MAIN FOR TESTING * //
	// ******************** //

	public static void main(String[] args) throws IOException {

		List<Object> testList = new ArrayList<>();
		int num = 1000000;
		for (int i = 0; i < num; i++) {
			testList.add(new Object());
		}

		List<Object> objects = new Vector<>();
		testList.parallelStream().forEach(h -> objects.add(h));

		System.out.println("{" + objects.size() + "," + num + "}");
	}
}
