package pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Vector;
import java.util.List;

import org.junit.jupiter.api.Test;

import hash.ImageHash;
import hash.implementations.PerceptualHash;
import pipeline.hasher.ImageHasher;
import utils.TestUtils;

public class ImageHasherTest {

	@Test
	void safeBooruSourceAndHasherTest() {

		int hashnum = 27;
		List<ImageHash> completedHashes = new Vector<>();

		ImageHasher safeHasher = new ImageHasher(TestUtils.safeScraper, new PerceptualHash(), 5, (hash) -> {
			completedHashes.add(hash);
		});

		List<ImageHash> hashhashes = safeHasher.hash(hashnum);

		try {
			assertEquals(hashhashes, completedHashes);
			assertTrue(completedHashes.size() == hashnum);
			for (ImageHash hash : hashhashes) {
				assertTrue(hash.getSource() != null);
			}
		} catch (AssertionError e) {
			System.err.println("CompletedHashes size: " + completedHashes.size());
			System.err.println("HashHashes size: " + hashhashes.size());
			e.printStackTrace(System.err);
			fail();
		}
	}
}
