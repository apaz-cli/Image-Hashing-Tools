package pipeline.sources.impl.loader;

public class LoadTask implements Runnable {

	ImageLoader owner;

	LoadTask(ImageLoader owner) {
		this.owner = owner;
	}

	@Override
	public void run() {
		owner.loadImage();
	}
}
