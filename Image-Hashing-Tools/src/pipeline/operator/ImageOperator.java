package pipeline.operator;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import image.IImage;
import pipeline.ImageSource;

public class ImageOperator implements ImageSource {

	private ImageSource source;
	private IImageOperation[] imageOperations;
	private int threadNum = 5;

	private Object closeLock = new Object();

	public ImageOperator(ImageSource source, IImageOperation operation) {
		this(source, new IImageOperation[] { operation });
	}

	public ImageOperator(ImageSource source, IImageOperation... operations) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(operations);
		for (IImageOperation op : operations) {
			Objects.requireNonNull(op);
		}

		this.source = source;
		this.imageOperations = operations;
	}

	public ImageOperator(ImageSource source, int threadNum, IImageOperation operation) {
		this(source, operation);
		this.threadNum = threadNum;
	}

	public ImageOperator(ImageSource source, int threadNum, IImageOperation... operations) {
		this(source, operations);
		this.threadNum = threadNum;
	}

	@Override
	public IImage<?> nextImage() {

		// Get image from backing source
		IImage<?> img = null;
		synchronized (this) {
			if (this.source == null) {
				return null;
			}
			img = source.nextImage();
		}

		if (img == null) {
			return null;
		}

		return applyOperations(img);
	}

	public IImage<?> applyOperations(IImage<?> img) {

		IImageOperation[] ops = null;
		synchronized (this) {
			// Synchronized so that it can't be closed during copy.

			// If closed already, do nothing.
			if (this.imageOperations == null) {
				return null;
			}

			// It seems strange to do a copy every time. You'd think that it would add up,
			// but this isn't actually very expensive at all.
			ops = Arrays.copyOf(this.imageOperations, this.imageOperations.length);
		}

		// Apply all the operations. Since we copied, this is now thread-safe.
		IImage<?> operated = null;
		for (IImageOperation op : ops) {
			operated = op.operate(img);
		}

		return operated;
	}

	/**
	 * Invokes the operations provided on all images in this source does not return
	 * a list. This is a terminal operation. It simply executes the operations on
	 * all the images in the ImageSource, or until close() is called.
	 */
	public void invokeAll() {
		synchronized (closeLock) {
			ExecutorService pool = Executors.newWorkStealingPool(this.threadNum);
			for (int i = 0; i < this.threadNum; i++) {
				pool.execute(() -> {
					@SuppressWarnings("unused")
					IImage<?> img;
					while ((img = this.nextImage()) != null) {
					}
				});
			}

			try {
				pool.shutdown();
				pool.awaitTermination(7, TimeUnit.DAYS);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public synchronized void close() {
		if (this.closeLock == null) {
			return;
		}
		synchronized (closeLock) {
			synchronized (this) {
				this.source = null;
				this.imageOperations = null;
			}
		}
		this.closeLock = null;
	}

}
