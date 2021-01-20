package app.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import app.actions.Actions;
import app.argparse.Options;
import app.util.TrackedMatch;
import hash.ImageHash;
import image.IImage;
import image.PixelUtils;
import image.implementations.RGBAImage;
import image.implementations.SourcedImage;

public class MergeWindow extends JFrame {
	private static final long serialVersionUID = -8718164576114861794L;
	static {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception ignored) {
		}
	}

	SourcedImage img1, img2;

	private int imageSideLength = 512;
	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	JLabel imageCanvas1 = new JLabel();
	JLabel imageCanvas2 = new JLabel();
	JLabel diffCanvas = new JLabel();
	Container pathContainer = new Container();
	TextField img1Path = new TextField();
	TextField img2Path = new TextField();

	TrackedMatch currentMatch = null;

	final Options options;

	// Pair<From, To>
	final List<TrackedMatch> toResolve;
	final Set<ImageHash> toDelete = new HashSet<>();

	// Hashes from the left side of the list of hashes to resolve came from
	// leftSource, and likewise on the right. Note that leftSource and rightSource
	// may point to the same source.
	public MergeWindow(final List<TrackedMatch> toResolve, Options options) {
		PixelUtils.assertNotNull(toResolve, options);

		this.options = options;
		this.toResolve = toResolve;
		if (toResolve.size() == 0) return;

		makeWindow();
		if (options.verbose) System.out.println("Exiting constructor.");

	}

	public void resolve() {
		for (TrackedMatch match : toResolve) {
			processMatch(match);
			try {
				synchronized (this) {
					this.wait();
				}
			} catch (InterruptedException e) {
			}
		}
	}

	public void destroy() {
		if (options.verbose) System.out.println("Disposing window.");
		this.dispose();

		if (options.verbose) System.out.println("Saving changes.");
		this.saveChanges();
	}

	private void processMatch(TrackedMatch match) {
		this.currentMatch = match;

		ImageHash h1 = match.getFirst();
		ImageHash h2 = match.getSecond();
		String s1 = h1.getSource();
		String s2 = h2.getSource();
		SourcedImage img1 = null, img2 = null;

		boolean fnf1 = false, fnf2 = false;
		try {
			img1 = h1.loadFromSource();
		} catch (IOException e) {
			img1 = getFNF(s1);
			fnf1 = true;
		}
		try {
			img2 = h2.loadFromSource();
		} catch (IOException e) {
			img2 = getFNF(s2);
			fnf2 = true;
		}

		this.updateImages(decorate(img1, h1), decorate(img2, h2));
		this.updateDiff(img1, img2, fnf1 || fnf2);
	}

	private void updateImages(SourcedImage img1, SourcedImage img2) {
		this.img1 = img1;
		this.img2 = img2;
		imageCanvas1.setIcon(iconify(img1, imageSideLength));
		imageCanvas2.setIcon(iconify(img2, imageSideLength));
		img1Path.setText(img1.getSource());
		img2Path.setText(img2.getSource());
	}

	private ImageIcon iconify(IImage<?> img, int sideLength) {
		return new ImageIcon(img.resizeBilinear(sideLength, sideLength).toBufferedImage());
	}

	private static RGBAImage FNF = null;
	private static RGBAImage FNFDecoration = null;

	private BufferedImage loadResource(String path) {
		// Get the image
		Image im = new ImageIcon(this.getClass().getResource(path).getFile()).getImage();

		// Convert to BufferedImage
		BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bi.createGraphics();
		bGr.drawImage(im, 0, 0, null);
		bGr.dispose();

		return bi;
	}

	private SourcedImage getFNF(String source) {
		if (FNF != null) return new SourcedImage(FNF, source, false);
		else {
			FNF = new RGBAImage(loadResource("/app/gui/FileNotFound.png"));
			return new SourcedImage(FNF, source, false);
		}
	}

	private RGBAImage getFNFDecoration() {
		if (FNFDecoration != null) return FNFDecoration;
		else {
			FNFDecoration = new RGBAImage(loadResource("/app/gui/FNFDecoration.png"));
			return FNFDecoration;
		}
	}

	private SourcedImage decorate(SourcedImage img, ImageHash h) {
		// TODO Mark the ones that are being kept.
		boolean isToBeDeleted = toDelete.contains(h);
		if (isToBeDeleted) {
			RGBAImage decorated = img.resizeBilinear(imageSideLength, imageSideLength).toRGBA()
					.emplaceSubimage(getFNFDecoration(), new Point(384, 384), new Point(511, 511));

			return new SourcedImage(decorated, img.getSource(), img.sourceIsURL());
		} else {
			return img;
		}
	}

	private void updateDiff(SourcedImage img1, SourcedImage img2, boolean fnf) {
		if (fnf) diffCanvas.setIcon(new ImageIcon(FNF.toBufferedImage()));
		else diffCanvas
				.setIcon(new ImageIcon(img1.imageDiff(img2, imageSideLength, imageSideLength).toBufferedImage()));
	}

	/***********/
	/* BUTTONS */
	/***********/

	private ActionListener keepImage1 = e -> {
		toDelete.add(currentMatch.getSecond());
		synchronized (this) {
			this.notify();
		}
	};
	private ActionListener keepImage2 = e -> {
		toDelete.add(currentMatch.getFirst());
		synchronized (this) {
			this.notify();
		}
	};
	private ActionListener keepBothIm = e -> {
		synchronized (this) {
			this.notify();
		}
	};
	private ActionListener keepLarger = e -> {
		if (img1.getArea() >= img2.getArea()) keepImage1.actionPerformed(e);
		else keepImage2.actionPerformed(e);
		// Let the other listener notify.
	};
	private ActionListener deleteBoth = e -> {
		toDelete.add(currentMatch.getFirst());
		toDelete.add(currentMatch.getSecond());
		synchronized (this) {
			this.notify();
		}
	};
	private ActionListener saveChanges = e -> {
		this.saveChanges();
		synchronized (this) {
			this.notify();
		}
	};

	private void saveChanges() {
		// Function is needed to access options member
		Actions.trashImages(new ArrayList<>(toDelete), options);
	}

	private void makeWindow() {
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(screenSize.width / 2, screenSize.height / 2);

		imageCanvas1.setHorizontalAlignment(SwingConstants.CENTER);
		imageCanvas2.setHorizontalAlignment(SwingConstants.CENTER);
		diffCanvas.setHorizontalAlignment(SwingConstants.CENTER);

		pathContainer.setLayout(new GridLayout(2, 1));
		pathContainer.add(img1Path);
		pathContainer.add(img2Path);

		Container cp = this.getContentPane();
		cp.add(makeButtonPanel(), BorderLayout.CENTER);
		cp.add(imageCanvas1, BorderLayout.WEST);
		cp.add(imageCanvas2, BorderLayout.EAST);
		cp.add(diffCanvas, BorderLayout.NORTH);
		cp.add(pathContainer, BorderLayout.PAGE_END);
		cp.setBackground(Color.decode("#2C2F33"));

		this.validate();
		this.setVisible(true);
	}

	private Panel makeButtonPanel() {
		Color buttonColor = Color.decode("#99AAB5");
		Font buttonFont = new Font("Monospaced", Font.BOLD, 18);

		Panel buttonPanel = new Panel();
		buttonPanel.setLayout(new GridLayout(3, 2));

		Button button1 = new Button("Keep 1");
		button1.addActionListener(keepImage1);
		button1.setBackground(buttonColor);
		button1.setFont(buttonFont);
		buttonPanel.add(button1);

		Button button2 = new Button("Keep 2");
		button2.addActionListener(keepImage2);
		button2.setBackground(buttonColor);
		button2.setFont(buttonFont);
		buttonPanel.add(button2);

		Button button3 = new Button("Keep Both");
		button3.addActionListener(keepBothIm);
		button3.setBackground(buttonColor);
		button3.setFont(buttonFont);
		buttonPanel.add(button3);

		Button button4 = new Button("Keep Larger");
		button4.addActionListener(keepLarger);
		button4.setBackground(buttonColor);
		button4.setFont(buttonFont);
		buttonPanel.add(button4);

		Button button5 = new Button("Delete Both");
		button5.addActionListener(deleteBoth);
		button5.setBackground(buttonColor);
		button5.setFont(buttonFont);
		buttonPanel.add(button5);

		Button button6 = new Button("Save Changes");
		button6.addActionListener(saveChanges);
		button6.setBackground(buttonColor);
		button6.setFont(buttonFont);
		buttonPanel.add(button6);

		return buttonPanel;
	}

}
