package pipeline.imagesources;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import image.IImage;

public class SourcedImage {

	private String source;
	private BufferedImage img;
	Boolean isURL;
	
	public SourcedImage(BufferedImage img, String source) {
		this.img = img;
		this.source = source;
	}
	
	public SourcedImage(BufferedImage img, String source, Boolean isURL) {
		this.img = img;
		this.source = source;
		this.isURL = isURL;
	}
	
	public SourcedImage(BufferedImage img, URL source) {
		this.img = img;
		this.source = source.toString();
		this.isURL = true;
	}
	
	public SourcedImage(BufferedImage img, File source) {
		this.img = img;
		this.source = source.toString();
		this.isURL = false;
	}
	
	public SourcedImage(IImage<?> img, String source) {
		this.img = img.toBufferedImage();
		this.source = source;
	}
	
	public SourcedImage(IImage<?> img, String source, Boolean isURL) {
		this.img = img.toBufferedImage();
		this.source = source;
		this.isURL = isURL;
	}
	
	public SourcedImage(IImage<?> img, URL source) {
		this.img = img.toBufferedImage();
		this.source = source.toString();
		this.isURL = true;
	}
	
	public SourcedImage(IImage<?> img, File source) {
		this.img = img.toBufferedImage();
		this.source = source.toString();
		this.isURL = false;
	}
	
	
	
	// Returns true if is a url, false if was a file, null if string.
	public boolean isURL() {
		if (this.isURL == null) {
			return this.isURL == true;	
		}
		return false;
	}
	
	public boolean isFile() {
		if (this.isURL == null) {
			return this.isURL == false;	
		}
		return false;
	}
	
	public boolean isUnknownOrigin() {
		return this.isURL == null;
	}
	
	public BufferedImage unwrap() {
		return this.img;
	}
	
	public String getSource() {
		return this.source;
	}
	
}
