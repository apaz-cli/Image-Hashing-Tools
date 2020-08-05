package app.gui;

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
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import image.IImage;
import image.PixelUtils;
import image.implementations.GreyscaleImage;
import image.implementations.SourcedImage;
import pipeline.ImageSource;
import pipeline.dedup.HashMatch;

public class MergeWindow extends JFrame {

	private static final long serialVersionUID = 8325320958239915522L;

	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	JLabel imageCanvas1 = new JLabel();
	JLabel imageCanvas2 = new JLabel();
	JLabel diffCanvas = new JLabel();

	List<HashMatch> matchesToResolve = null;
	SourcedImage img1 = null, img2 = null;
	ImageSource leftSource = null, rightSource = null;

	static {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception ignored) {
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			SwingUtilities.invokeAndWait(() -> new MergeWindow(null, null, null));
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Hashes from the left side of the list of hashes to resolve came from
	// leftSource, and likewise on the right. Note that leftSource and rightSource
	// may point to the same source.
	public MergeWindow(List<HashMatch> toResolve, ImageSource leftSource, ImageSource rightSource) {
		PixelUtils.assertNotNull(toResolve, leftSource, rightSource);
		this.matchesToResolve = toResolve;
		this.leftSource = leftSource;
		this.rightSource = rightSource;

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

		Button button5 = new Button("Delete both");
		button5.addActionListener(deleteBoth);
		button5.setBackground(buttonColor);
		button5.setFont(buttonFont);
		buttonPanel.add(button5);

		return buttonPanel;
	}

	private ImageIcon resizeForCanvas(IImage<?> img, int sideLength) {
		return new ImageIcon(img.resizeBilinear(sideLength, sideLength).toBufferedImage());
	}

	private void nextMatch() {
		if (matchesToResolve.isEmpty()) {
			System.exit(0);
		}

		HashMatch match = matchesToResolve.remove(matchesToResolve.size() - 1);
		try {
			img1 = match.loadFirst();
			img2 = match.loadSecond();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.updateImages(img1 != null ? img1 : new GreyscaleImage(new byte[1], 1, 1),
				img2 != null ? img2 : new GreyscaleImage(new byte[1], 1, 1));
	}

	protected void updateImages(IImage<?> img1, IImage<?> img2) {
		int imageSideLength = 512;
		imageCanvas1.setIcon(resizeForCanvas(img1, imageSideLength));
		imageCanvas2.setIcon(resizeForCanvas(img2, imageSideLength));
		diffCanvas.setIcon(new ImageIcon(img1.imageDiff(img2, imageSideLength, imageSideLength).toBufferedImage()));
	}

	ActionListener keepImage1 = e -> {};
	ActionListener keepImage2 = e -> {};
	ActionListener keepBothIm = e -> {};
	ActionListener keepLarger = e -> {};
	ActionListener deleteBoth = e -> {};

}
