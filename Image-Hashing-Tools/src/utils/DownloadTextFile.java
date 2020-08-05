package utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import image.PixelUtils;

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

					String urlFilePath = url.getFile();
					String[] pathSpt = urlFilePath.split("/");
					String nameAndExt = pathSpt[pathSpt.length - 1];
					String ext = formatName(nameAndExt);

					File namedFile = avoidNameCollision(dlFolderName + File.separator + nameAndExt, ext);

					ImageIO.write(img, ext == "" ? "png" : ext, namedFile);
				} catch (IOException e) {
				} catch (Exception e) {
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

	private static Set<String> suffixes = new HashSet<>(Arrays.asList(ImageIO.getWriterFileSuffixes()));

	public static String formatName(String name) {
		int idx = name.lastIndexOf('.');
		if (idx > 0) {
			return name.substring(idx + 1);
		} else {
			return "";
		}
	}

	public static String formatName(File f) {
		return formatName(f.toString());
	}

	public static boolean formatSupported(File f) {
		return formatSupported(formatName(f));
	}

	public static boolean formatSupported(String formatName) {
		return suffixes.contains(formatName);
	}

	public static File avoidNameCollision(String name, String formatName) {
		PixelUtils.assertNotNull(name, formatName);
		if (formatName.startsWith(".") && formatName.length() > 1) formatName = formatName.substring(1);
		if (!formatSupported(formatName)) throw new IllegalArgumentException(
				"File format " + formatName + " not supported. Supported formats: " + suffixes);
		return avoidNameCollision(name, formatName, false);
	}

	private static File avoidNameCollision(String name, String formatName, boolean changed) {

		int i = name.lastIndexOf('.');
		if (i > 0) {
			name = name.substring(0, i);
		}

		File f = new File(name + "." + formatName);
		if (f.exists() || !f.isDirectory()) return f;

		if (!changed) return avoidNameCollision(name + " (1)." + formatName, formatName, true);
		else {
			String beforeNumber = null;
			long number;
			String afterNumber = null;

			i = name.lastIndexOf('(');
			if (i > 0) {
				beforeNumber = name.substring(0, i + 1);
			}

			int j = name.lastIndexOf(')');
			if (j > 0) {
				afterNumber = name.substring(j, name.length());
			}

			number = Integer.parseInt(name.substring(i + 1, j));

			return avoidNameCollision(beforeNumber + (number + 1) + afterNumber, formatName, true);
		}
	}

}
