package hash;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import hash.ImageHash;
import hash.implementations.DifferenceHash;
import utils.BenchmarkRunner;

public class ImageHashTest {

	@Test
	void serializeTest() {

		ImageHash h1 = null, h2 = null;
		try {
			h1 = new DifferenceHash().hash(BenchmarkRunner.IMAGES.get(0));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			File serialized = new File("serialized.hash");
			h1.writeToNewFile(serialized);
			h2 = ImageHash.fromFile(serialized);
			serialized.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		assertEquals(h1, h2);
	}

	@Test
	void toFromStringTest() {
		ImageHash h = null;
		try {
			h = new DifferenceHash().hash(BenchmarkRunner.IMAGES.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(h, ImageHash.fromString(h.toString()));
	}

}
