package pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Vector;
import java.util.List;

import org.junit.jupiter.api.Test;

import hash.ImageHash;
import hash.implementations.PerceptualHash;
import pipeline.hasher.ImageHasher;
import pipeline.sources.impl.safebooruscraper.SafebooruScraper;

public class ImageHasherTest {

	@Test
	void safeSourceHasherTest() {

		int hashnum = 37;
		List<ImageHash> completedHashes = new Vector<>();
		
		ImageHasher safeHasher = new ImageHasher(new SafebooruScraper(), new PerceptualHash(), 5, (hash) -> {
			completedHashes.add(hash);
		});

		List<ImageHash> hashhashes = safeHasher.hash(hashnum);

		try {
			assertEquals(hashhashes, completedHashes);
			assertTrue(completedHashes.size() == hashnum);
		} catch (AssertionError e) {
			System.err.println("CompletedHashes size: " + completedHashes.size());
			System.err.println("HashHashes size: " + hashhashes.size());
			throw e;
		}
	}
}
