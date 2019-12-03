package pipeline.sources.impl.downloader;

class DownloadTask implements Runnable {
	
	private final URLCollectionDownloader taskOwner;

	public DownloadTask(URLCollectionDownloader taskOwner) {
		this.taskOwner = taskOwner;
	}

	@Override
	public void run() {
		taskOwner.downloadImage();
	}
}