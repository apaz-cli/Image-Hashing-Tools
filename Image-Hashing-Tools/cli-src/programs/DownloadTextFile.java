package programs;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import utils.ImageUtils;

public class DownloadTextFile {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println(
					"Please provide a single argument to this program, the path to a file of links to download.");
			System.exit(0);
		}

		final String dlFolderName = System.getProperty("user.home") + "/Downloads/BHDownloads";
		new File(dlFolderName).mkdirs();

		List<URL> urlsFromFile = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
			String line;
			while ((line = br.readLine()) != null) {
				urlsFromFile.add(new URL(line));
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the file: " + args[0]);
			System.exit(0);
		} catch (IOException e) {
			System.err.println("Oops. That wasn't supposed to happen. Please give me the following stack trace.");
			e.printStackTrace();
			System.exit(1);
		}

		final List<URL> failedDownloads = new Vector<>();
		ExecutorService threadPool = Executors.newWorkStealingPool(10);
		List<Callable<Void>> tasks = new ArrayList<>();

		for (URL url : urlsFromFile) {
			tasks.add(() -> {
				try {
					BufferedImage img = ImageUtils.openImage(url);

					File namedFile = ImageUtils.avoidNameCollision(new File(dlFolderName));
					String ext = ImageUtils.formatName(namedFile);

					ImageIO.write(img, ext == "" ? "png" : ext, namedFile);
				} catch (IOException e) {} catch (Exception e) {
					failedDownloads.add(url);
				}
				return null;
			});
		}
		try {
			threadPool.invokeAll(tasks);
			threadPool.shutdown();
			threadPool.awaitTermination(5, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Could not parse the following images. "
				+ "Try opening these links in your browser and see if you can scavenge any. "
				+ "These are the images that could not be loaded, most likely due to a bug in java.");
		failedDownloads.stream().forEach(url -> System.out.println(url));

	}

}
