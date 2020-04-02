package hashstore;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

import hashstore.euclidean.NdArray;

public class NdArrayTest {

	@Test
	void indexTest() {
		Float[] data = new Float[5 * 4 * 3 * 2];
		Float[] exdata = new Float[5 * 4 * 3 * 2];
		
		NdArray<Float> nd = new NdArray<>(data, new int[] { 5, 4, 3, 2 });
		int offset = 0;
		Random r = new Random();
		float f;

		// Assert that the data is being stored in the right places
		for (int a = 0; a < 5; a++) {
			for (int b = 0; b < 4; b++) {
				for (int c = 0; c < 3; c++) {
					for (int d = 0; d < 2; d++) {
						f = r.nextFloat();
						exdata[offset++] = f;
						nd.set(f, a, b, c, d);
						assertEquals(f, nd.get(a, b, c, d));
					}
				}
			}
		}

		// Assert that all the data has been stored
		data = Arrays.copyOf(data, data.length);
		Arrays.sort(data, 0, data.length);
		Arrays.sort(exdata, 0, exdata.length);
		assertTrue(Arrays.equals(data, exdata));
	}

	@Test
	void throwTests() {
		assertThrows(java.lang.IllegalArgumentException.class, () -> {
			new NdArray<>(99999, 99999, 99999);
		});
		assertThrows(java.lang.IllegalArgumentException.class, () -> {
			new NdArray<>(-1, 5, 6, 3, 2, 4);
		});
		assertThrows(java.lang.IllegalArgumentException.class, () -> {
			new NdArray<>(null);
		});
		assertThrows(java.lang.NullPointerException.class, () -> {
			new NdArray<>(5, 5, 5).get(null);
		});
		assertThrows(java.lang.ArrayIndexOutOfBoundsException.class, () -> {
			new NdArray<>(5, 5, 5).get(0, 0, 5);
		});
	}
}
