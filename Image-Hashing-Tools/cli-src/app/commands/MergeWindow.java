package app.commands;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;

import hash.ImageHash;
import image.IImage;
import image.PixelUtils;
import image.implementations.SourcedImage;
import pipeline.ImageSource;
import pipeline.dedup.HashMatch;

public class MergeWindow extends JFrame {

	private static final long serialVersionUID = 8325320958239915522L;

	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	JLabel imageCanvas1 = new JLabel();
	JLabel imageCanvas2 = new JLabel();
	JLabel diffCanvas = new JLabel();

	SourcedImage img1 = null, img2 = null;
	ImageSource[] sources;

	List<HashMatch> partialMatches = null;
	HashMatch currentMatch = null;
	Command callingCommand;

	List<ImageHash> toDelete = new ArrayList<>();

	static {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception ignored) {}
	}

	// Hashes from the left side of the list of hashes to resolve came from
	// leftSource, and likewise on the right. Note that leftSource and rightSource
	// may point to the same source.
	MergeWindow(Command callingCommand, List<HashMatch> toResolve, ImageSource[] sources) {
		PixelUtils.assertNotNull(toResolve, sources);
		this.callingCommand = callingCommand;
		this.partialMatches = toResolve;
		this.sources = sources;

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(screenSize.width / 2, screenSize.height / 2);

		imageCanvas1.setHorizontalAlignment(JLabel.CENTER);
		imageCanvas2.setHorizontalAlignment(JLabel.CENTER);
		diffCanvas.setHorizontalAlignment(JLabel.CENTER);

		Container cp = this.getContentPane();
		cp.add(makeButtonPanel(), BorderLayout.CENTER);
		cp.add(imageCanvas1, BorderLayout.WEST);
		cp.add(imageCanvas2, BorderLayout.EAST);
		cp.add(diffCanvas, BorderLayout.NORTH);
		cp.setBackground(Color.decode("#2C2F33"));

		this.nextMatch();
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
		buttonPanel.add(button5);

		return buttonPanel;
	}

	private ImageIcon resizeForCanvas(IImage<?> img, int sideLength) {
		return new ImageIcon(img.resizeBilinear(sideLength, sideLength).toBufferedImage());
	}

	private void nextMatch() {
		if (partialMatches.isEmpty()) {
			System.exit(0);
		}

		String err = null;
		HashMatch match = partialMatches.remove(partialMatches.size() - 1);
		try {
			ImageHash h1 = match.getFirst();
			err = h1.getSource();
			this.img1 = h1.loadFromSource();

			ImageHash h2 = match.getSecond();
			err = h2.getSource();
			this.img1 = h2.loadFromSource();
		} catch (IOException e) {
			System.err.println(err);
			e.printStackTrace();
			System.exit(2);
		}

		currentMatch = match;
		this.updateImages(img1, img2);
	}

	protected void updateImages(IImage<?> img1, IImage<?> img2) {
		int imageSideLength = 512;
		imageCanvas1.setIcon(resizeForCanvas(img1, imageSideLength));
		imageCanvas2.setIcon(resizeForCanvas(img2, imageSideLength));
		diffCanvas.setIcon(new ImageIcon(img1.imageDiff(img2, imageSideLength, imageSideLength).toBufferedImage()));
	}

	ActionListener keepImage1 = e -> {
		synchronized (callingCommand) {
			toDelete.add(currentMatch.getSecond());
			nextMatch();
		}
	};
	ActionListener keepImage2 = e -> {
		synchronized (callingCommand) {
			toDelete.add(currentMatch.getFirst());
			nextMatch();
		}
	};
	ActionListener keepBothIm = e -> {
		synchronized (callingCommand) {
			nextMatch();
		}
	};
	ActionListener keepLarger = e -> {
		synchronized (callingCommand) {
			if (img1.getArea() >= img2.getArea()) {
				keepImage1.actionPerformed(e);
			} else {
				keepImage2.actionPerformed(e);
			}
		}
	};
	ActionListener deleteBoth = e -> {
		synchronized (callingCommand) {
			toDelete.add(currentMatch.getFirst());
			toDelete.add(currentMatch.getSecond());
			nextMatch();
		}
	};
	
	// Leave the action of actually saving to the calling method.
	ActionListener saveChanges = e -> {
		synchronized(callingCommand) {
			this.dispose();
		}
	};
	
	public Set<ImageHash> getToDelete() {
		return new HashSet<>(this.toDelete);
	}

}
