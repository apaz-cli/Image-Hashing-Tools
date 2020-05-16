package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

import hash.*;
import hash.implementations.*;
import image.*;
import image.implementations.*;
import pipeline.ImageSource;
import pipeline.hasher.HasherOutput;
import pipeline.hasher.ImageHasher;
import pipeline.operator.IImageOperation;
import pipeline.operator.ImageOperator;
import pipeline.sources.*;
import pipeline.sources.downloader.URLCollectionDownloader;
import pipeline.sources.loader.ImageLoader;
import pipeline.sources.safebooruscraper.SafebooruScraper;
import attack.*;
import attack.convolutions.*;
import attack.other.*;

@SuppressWarnings("unused")
public class BenchmarkRunner {

	// ******************** //
	// * MAIN FOR TESTING * //
	// ******************** //

	public static void main(String[] args) {
		SeperableKernel ker = KernelFactory.averageBlurKernel(3, EdgeMode.WRAP);

		ImageSource scraper = new SafebooruScraper();
		IImage<?> img = scraper.nextImage();

		ImageUtils.showImage(img);
		ImageUtils.showImage(img.apply(ker));
		scraper.close();
		
		ImageHash h = img.hash(new DifferenceHash(32));
		
		System.out.println(h.equals(ImageHash.fromString(h.toString())));
	}

}
