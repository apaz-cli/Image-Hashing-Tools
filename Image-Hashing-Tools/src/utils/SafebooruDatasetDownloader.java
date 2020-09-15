package utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

import pipeline.sources.SafebooruScraper;

public class SafebooruDatasetDownloader {

	public static void main(String[] args) {
		if (args.length > 1) {
			System.out.println(
					"Expected 0-1 arguments, the folder to download the dataset into or a new folder named Dataset in the same folder as this jar file if not provided.");
			System.exit(1);
		}

		File downloadDir = null;
		if (args.length == 1) {
			downloadDir = new File(args[0]);
		} else {
			try {
				File thisJar = new File(
						SafebooruDatasetDownloader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				downloadDir = new File(thisJar.getParent(), "TrainDataset");
				downloadDir.mkdir();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}

		class IntBox {
			AtomicInteger i = new AtomicInteger(1);
		}
		final IntBox iBox = new IntBox();
		final File dir = downloadDir;
		new SafebooruScraper(SafebooruScraper.PREVIEW_URL).parallelStream().map(img -> img.resizeBilinear(64, 64))
				.forEach(img -> {
					try {
						img.save(new File(dir, (iBox.i.getAndIncrement()) + ".png"), "png");
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(2);
					}
				});

		System.out.println("All of Safebooru has been updated. Downloaded " + (iBox.i.get() - 1)
				+ " images. Please reserve validation data by running the dataset splitting utility.");
	}

}
