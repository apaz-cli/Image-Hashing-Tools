package pipeline.imagesources.safebooruscraper;

public class PageRequestResult {

	private int finalCount;
	private int currentPage;
	private boolean trackIsFinished;

	PageRequestResult(int currentPage, int finalCount, boolean trackIsFinished) {
		this.currentPage = currentPage;
		this.finalCount = finalCount;
		this.trackIsFinished = trackIsFinished;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getFinalCount() {
		return finalCount;
	}

	public boolean isTrackFinished() {
		return trackIsFinished;
	}
}
