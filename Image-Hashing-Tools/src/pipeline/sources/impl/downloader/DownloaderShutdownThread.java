package pipeline.sources.impl.downloader;

public class DownloaderShutdownThread extends Thread {
	
	URLCollectionDownloader loader;
	
	public DownloaderShutdownThread(URLCollectionDownloader downloader) {
		this.loader = downloader;
	}
	
	@Override
	public void run() {
		loader.shutdownPool();
    }
}
