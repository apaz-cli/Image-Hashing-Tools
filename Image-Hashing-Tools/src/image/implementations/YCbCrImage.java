package image.implementations;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import image.IImage;

public class YCbCrImage implements IImage<YCbCrImage> {

	// Luminance values are precomputed since they will be most used.
	private static float[] luminanceWeightR = new float[] { 0.0f, 0.25678906f, 0.5135781f, 0.77036715f, 1.0271562f,
			1.2839453f, 1.5407343f, 1.7975234f, 2.0543125f, 2.3111014f, 2.5678906f, 2.8246796f, 3.0814686f, 3.3382578f,
			3.5950468f, 3.851836f, 4.108625f, 4.365414f, 4.622203f, 4.878992f, 5.1357813f, 5.39257f, 5.649359f,
			5.9061484f, 6.162937f, 6.4197264f, 6.6765156f, 6.933305f, 7.1900935f, 7.4468827f, 7.703672f, 7.9604607f,
			8.21725f, 8.474039f, 8.730828f, 8.9876175f, 9.244406f, 9.501195f, 9.757984f, 10.014773f, 10.271563f,
			10.528352f, 10.78514f, 11.041929f, 11.298718f, 11.555508f, 11.812297f, 12.069086f, 12.325874f, 12.582664f,
			12.839453f, 13.096242f, 13.353031f, 13.60982f, 13.86661f, 14.123398f, 14.380187f, 14.636976f, 14.893765f,
			15.150555f, 15.407344f, 15.664132f, 15.920921f, 16.177711f, 16.4345f, 16.691288f, 16.948078f, 17.204866f,
			17.461657f, 17.718445f, 17.975235f, 18.232023f, 18.488811f, 18.745602f, 19.00239f, 19.25918f, 19.515968f,
			19.772757f, 20.029547f, 20.286335f, 20.543125f, 20.799913f, 21.056704f, 21.313492f, 21.57028f, 21.82707f,
			22.083858f, 22.340649f, 22.597437f, 22.854227f, 23.111015f, 23.367804f, 23.624594f, 23.881382f, 24.138172f,
			24.39496f, 24.651749f, 24.908539f, 25.165327f, 25.422117f, 25.678905f, 25.935696f, 26.192484f, 26.449272f,
			26.706062f, 26.96285f, 27.21964f, 27.476429f, 27.73322f, 27.990007f, 28.246796f, 28.503586f, 28.760374f,
			29.017164f, 29.273952f, 29.530743f, 29.78753f, 30.04432f, 30.30111f, 30.557898f, 30.814688f, 31.071476f,
			31.328264f, 31.585054f, 31.841843f, 32.098633f, 32.355423f, 32.61221f, 32.869f, 33.12579f, 33.382576f,
			33.639366f, 33.896156f, 34.152946f, 34.409733f, 34.666523f, 34.923313f, 35.1801f, 35.43689f, 35.69368f,
			35.95047f, 36.207256f, 36.464046f, 36.720837f, 36.977623f, 37.234413f, 37.491203f, 37.747993f, 38.00478f,
			38.26157f, 38.51836f, 38.775146f, 39.031937f, 39.288727f, 39.545513f, 39.802303f, 40.059093f, 40.315884f,
			40.57267f, 40.82946f, 41.08625f, 41.343037f, 41.599827f, 41.856617f, 42.113407f, 42.370193f, 42.626984f,
			42.883774f, 43.14056f, 43.39735f, 43.65414f, 43.91093f, 44.167717f, 44.424507f, 44.681297f, 44.938084f,
			45.194874f, 45.451664f, 45.708454f, 45.96524f, 46.22203f, 46.47882f, 46.735607f, 46.992397f, 47.249187f,
			47.505978f, 47.762764f, 48.019554f, 48.276344f, 48.53313f, 48.78992f, 49.04671f, 49.303497f, 49.560287f,
			49.817078f, 50.073868f, 50.330654f, 50.587444f, 50.844234f, 51.10102f, 51.35781f, 51.6146f, 51.87139f,
			52.128178f, 52.384968f, 52.641758f, 52.898544f, 53.155334f, 53.412125f, 53.668915f, 53.9257f, 54.18249f,
			54.43928f, 54.696068f, 54.952858f, 55.20965f, 55.46644f, 55.723225f, 55.980015f, 56.236805f, 56.49359f,
			56.75038f, 57.00717f, 57.26396f, 57.52075f, 57.77754f, 58.03433f, 58.291115f, 58.547905f, 58.804695f,
			59.061485f, 59.31827f, 59.57506f, 59.831852f, 60.08864f, 60.34543f, 60.60222f, 60.859005f, 61.115795f,
			61.372585f, 61.629375f, 61.88616f, 62.142952f, 62.399742f, 62.65653f, 62.91332f, 63.17011f, 63.4269f,
			63.683685f, 63.940475f, 64.197266f, 64.454056f, 64.710846f, 64.96763f, 65.22442f, 65.48121f };
	private static float[] luminanceWeightG = new float[] { 0.0f, 0.50412893f, 1.0082579f, 1.5123868f, 2.0165157f,
			2.5206447f, 3.0247736f, 3.5289025f, 4.0330315f, 4.5371604f, 5.0412893f, 5.5454183f, 6.049547f, 6.553676f,
			7.057805f, 7.561934f, 8.066063f, 8.570192f, 9.074321f, 9.578449f, 10.082579f, 10.586708f, 11.090837f,
			11.594965f, 12.099094f, 12.603224f, 13.107352f, 13.611481f, 14.11561f, 14.61974f, 15.123868f, 15.627996f,
			16.132126f, 16.636255f, 17.140385f, 17.644512f, 18.148642f, 18.652771f, 19.156898f, 19.661028f, 20.165157f,
			20.669287f, 21.173416f, 21.677544f, 22.181673f, 22.685802f, 23.18993f, 23.69406f, 24.198189f, 24.702318f,
			25.206448f, 25.710575f, 26.214705f, 26.718834f, 27.222961f, 27.72709f, 28.23122f, 28.73535f, 29.23948f,
			29.743607f, 30.247736f, 30.751865f, 31.255993f, 31.760122f, 32.26425f, 32.76838f, 33.27251f, 33.776638f,
			34.28077f, 34.784897f, 35.289024f, 35.793156f, 36.297283f, 36.80141f, 37.305542f, 37.80967f, 38.313797f,
			38.81793f, 39.322056f, 39.826187f, 40.330315f, 40.834442f, 41.338573f, 41.8427f, 42.346832f, 42.85096f,
			43.355087f, 43.85922f, 44.363346f, 44.867474f, 45.371605f, 45.875732f, 46.37986f, 46.88399f, 47.38812f,
			47.89225f, 48.396378f, 48.900505f, 49.404636f, 49.908764f, 50.412895f, 50.917023f, 51.42115f, 51.92528f,
			52.42941f, 52.933537f, 53.437668f, 53.941795f, 54.445923f, 54.950054f, 55.45418f, 55.958313f, 56.46244f,
			56.966568f, 57.4707f, 57.974827f, 58.47896f, 58.983086f, 59.487213f, 59.991344f, 60.495472f, 60.9996f,
			61.50373f, 62.00786f, 62.511986f, 63.016117f, 63.520245f, 64.024376f, 64.5285f, 65.03263f, 65.53676f,
			66.04089f, 66.54502f, 67.04915f, 67.553276f, 68.0574f, 68.56154f, 69.06567f, 69.56979f, 70.07392f,
			70.57805f, 71.08218f, 71.58631f, 72.09044f, 72.59457f, 73.098694f, 73.60282f, 74.10696f, 74.611084f,
			75.11521f, 75.61934f, 76.12347f, 76.627594f, 77.13173f, 77.63586f, 78.139984f, 78.64411f, 79.14824f,
			79.652374f, 80.1565f, 80.66063f, 81.16476f, 81.668884f, 82.17302f, 82.67715f, 83.181274f, 83.6854f,
			84.18953f, 84.693665f, 85.19779f, 85.70192f, 86.20605f, 86.710175f, 87.2143f, 87.71844f, 88.222565f,
			88.72669f, 89.23082f, 89.73495f, 90.23908f, 90.74321f, 91.24734f, 91.751465f, 92.25559f, 92.75972f,
			93.263855f, 93.76798f, 94.27211f, 94.77624f, 95.280365f, 95.7845f, 96.28863f, 96.792755f, 97.29688f,
			97.80101f, 98.305145f, 98.80927f, 99.3134f, 99.81753f, 100.321655f, 100.82579f, 101.32992f, 101.834045f,
			102.33817f, 102.8423f, 103.34643f, 103.85056f, 104.35469f, 104.85882f, 105.362946f, 105.86707f, 106.37121f,
			106.875336f, 107.37946f, 107.88359f, 108.38772f, 108.891846f, 109.39598f, 109.90011f, 110.404236f,
			110.90836f, 111.41249f, 111.916626f, 112.42075f, 112.92488f, 113.42901f, 113.933136f, 114.43727f, 114.9414f,
			115.445526f, 115.94965f, 116.45378f, 116.95792f, 117.46204f, 117.96617f, 118.4703f, 118.97443f, 119.47855f,
			119.98269f, 120.48682f, 120.990944f, 121.49507f, 121.9992f, 122.503334f, 123.00746f, 123.51159f, 124.01572f,
			124.519844f, 125.02397f, 125.52811f, 126.032234f, 126.53636f, 127.04049f, 127.54462f, 128.04875f,
			128.55287f };
	private static float[] luminanceWeightB = new float[] { 0.0f, 0.09790625f, 0.1958125f, 0.29371876f, 0.391625f,
			0.48953122f, 0.5874375f, 0.68534374f, 0.78325f, 0.8811562f, 0.97906244f, 1.0769687f, 1.174875f, 1.2727813f,
			1.3706875f, 1.4685937f, 1.5665f, 1.6644062f, 1.7623124f, 1.8602186f, 1.9581249f, 2.0560312f, 2.1539373f,
			2.2518437f, 2.34975f, 2.4476562f, 2.5455625f, 2.6434686f, 2.741375f, 2.839281f, 2.9371874f, 3.0350935f,
			3.133f, 3.2309062f, 3.3288124f, 3.4267187f, 3.5246248f, 3.6225312f, 3.7204373f, 3.8183436f, 3.9162498f,
			4.0141563f, 4.1120625f, 4.2099686f, 4.3078747f, 4.4057813f, 4.5036874f, 4.6015935f, 4.6995f, 4.797406f,
			4.8953123f, 4.9932184f, 5.091125f, 5.189031f, 5.286937f, 5.3848433f, 5.48275f, 5.580656f, 5.678562f,
			5.7764688f, 5.874375f, 5.972281f, 6.070187f, 6.1680937f, 6.266f, 6.363906f, 6.4618125f, 6.5597186f,
			6.6576247f, 6.755531f, 6.8534374f, 6.9513435f, 7.0492496f, 7.1471562f, 7.2450624f, 7.3429685f, 7.4408746f,
			7.538781f, 7.6366873f, 7.7345934f, 7.8324995f, 7.930406f, 8.028313f, 8.126219f, 8.224125f, 8.322031f,
			8.419937f, 8.517843f, 8.615749f, 8.713656f, 8.811563f, 8.909469f, 9.007375f, 9.105281f, 9.203187f,
			9.301093f, 9.399f, 9.496906f, 9.594812f, 9.6927185f, 9.790625f, 9.888531f, 9.986437f, 10.084343f, 10.18225f,
			10.280156f, 10.378062f, 10.475968f, 10.573874f, 10.671781f, 10.769687f, 10.867594f, 10.9655f, 11.063406f,
			11.161312f, 11.259218f, 11.357124f, 11.45503f, 11.5529375f, 11.650844f, 11.74875f, 11.846656f, 11.944562f,
			12.042468f, 12.140374f, 12.238281f, 12.336187f, 12.434093f, 12.532f, 12.629906f, 12.727812f, 12.825718f,
			12.923625f, 13.021531f, 13.119437f, 13.217343f, 13.315249f, 13.413156f, 13.511062f, 13.608969f, 13.706875f,
			13.804781f, 13.902687f, 14.000593f, 14.098499f, 14.196405f, 14.2943125f, 14.392219f, 14.490125f, 14.588031f,
			14.685937f, 14.783843f, 14.881749f, 14.979656f, 15.077562f, 15.175468f, 15.273375f, 15.371281f, 15.469187f,
			15.567093f, 15.664999f, 15.762906f, 15.860812f, 15.958718f, 16.056625f, 16.154531f, 16.252438f, 16.350344f,
			16.44825f, 16.546156f, 16.644062f, 16.741968f, 16.839874f, 16.93778f, 17.035686f, 17.133593f, 17.231499f,
			17.329405f, 17.427313f, 17.525219f, 17.623125f, 17.721031f, 17.818937f, 17.916843f, 18.01475f, 18.112656f,
			18.210562f, 18.308468f, 18.406374f, 18.50428f, 18.602186f, 18.700092f, 18.798f, 18.895906f, 18.993813f,
			19.091719f, 19.189625f, 19.28753f, 19.385437f, 19.483343f, 19.58125f, 19.679155f, 19.777061f, 19.874968f,
			19.972874f, 20.07078f, 20.168686f, 20.266594f, 20.3645f, 20.462406f, 20.560312f, 20.658218f, 20.756124f,
			20.85403f, 20.951937f, 21.049843f, 21.147749f, 21.245655f, 21.343561f, 21.441467f, 21.539373f, 21.637281f,
			21.735188f, 21.833094f, 21.931f, 22.028906f, 22.126812f, 22.224718f, 22.322624f, 22.42053f, 22.518436f,
			22.616343f, 22.714249f, 22.812155f, 22.91006f, 23.007969f, 23.105875f, 23.203781f, 23.301687f, 23.399593f,
			23.4975f, 23.595406f, 23.693312f, 23.791218f, 23.889124f, 23.98703f, 24.084936f, 24.182842f, 24.280748f,
			24.378656f, 24.476562f, 24.574469f, 24.672375f, 24.77028f, 24.868187f, 24.966093f };

	public static GreyscaleImage computeY(BufferedImage img) {
		return computeY(new RGBImage(img));
	}

	public static GreyscaleImage computeY(IImage<?> img) {

		RGBImage image = img.toRGB();
		byte[] red = image.getRed().getPixels();
		byte[] green = image.getGreen().getPixels();
		byte[] blue = image.getBlue().getPixels();

		int width = image.getWidth();
		int height = image.getHeight();

		byte[] y = new byte[width * height];
		float j;

		//@nof
		for (int i = 0; i < red.length; i++) {
			j = 16 + luminanceWeightR[red[i] & 0xff] 
				   + luminanceWeightG[green[i] & 0xff] 
				   + luminanceWeightB[blue[i] & 0xff];
			y[i] = (byte) ((j > 255 ? 
					255f : 
					j < 0 ? 0f : j) + .5f);
		}
		//@dof

		return new GreyscaleImage(y, width, height);
	}

	// Luminance
	private GreyscaleImage Y;
	// Chrominance in the direction of blue
	private GreyscaleImage Cb;
	// Chrominance in the direction of red
	private GreyscaleImage Cr;
	private int width;
	private int height;

	public YCbCrImage(RGBImage rgb) {
		byte[] red = rgb.getRed().getPixels();
		byte[] green = rgb.getGreen().getPixels();
		byte[] blue = rgb.getBlue().getPixels();

		this.width = rgb.getWidth();
		this.height = rgb.getHeight();

		byte[] y = new byte[this.width * this.height];
		byte[] cb = new byte[this.width * this.height];
		byte[] cr = new byte[this.width * this.height];

		// @nof
		int r, g, b;
		float j, k, l;
		for (int i = 0; i < red.length; i++) {
			// Extract workable integer
			r = red[i] & 0xff;
			g = green[i] & 0xff;
			b = blue[i] & 0xff;

			// Convert RGB to YCbCr
			// Equations can be found at https://sistenix.com/rgb2ycbcr.html
			// Specifically at: https://sistenix.com/img/rgb_eq1.svg\
			
			// Luminance values are precomputed since they will be most used. 
			// For now, the others are not.
			
			j = 16 + luminanceWeightR[r] 
				   + luminanceWeightG[g] 
				   + luminanceWeightB[b];
			// Cb
			k = 128 
					- (r * (37.945f / 256)) 
					- (g * (74.494f / 256)) 
					+ (b * (112.439f / 256));
			// Cr
			l = 128 
					+ (r * (112.439f / 256)) 
					- (g * (94.154f / 256)) 
					- (b * (18.285f / 256));
			
			// Validate
			if (j > 255) {j = 255f;} else if (j < 0) {j = 0f;}
			if (k > 255) {k = 255f;} else if (k < 0) {k = 0f;}
			if (l > 255) {l = 255f;} else if (l < 0) {l = 0f;}
			
			// Round and store values. Casting to byte truncates, 
			// and by adding .5, this is basically an implementation 
			// of Math.round(), that only works for positive numbers.
			// However, as we have validated 0 <= j, k, l <= 255 
			// this is fine.
			y[i] = (byte) (j + .5f);
			cb[i] = (byte) (k + .5f);
			cr[i] = (byte) (l + .5f);
		}
		// @dof

		this.Y = new GreyscaleImage(y, this.width, this.height);
		this.Cb = new GreyscaleImage(cb, this.width, this.height);
		this.Cr = new GreyscaleImage(cr, this.width, this.height);
	}

	// Y, Cb, Cr become backing
	public YCbCrImage(GreyscaleImage Y, GreyscaleImage Cb, GreyscaleImage Cr) throws IllegalArgumentException {
		int len = Y.getPixels().length;
		if (len != Cb.getPixels().length || len != Cr.getPixels().length) {
			throw new IllegalArgumentException("All three images must be the same size.");
		}

		this.Y = Y;
		this.Cb = Cb;
		this.Cr = Cr;
		this.width = Y.getWidth();
		this.height = Y.getHeight();
	}

	public YCbCrImage(BufferedImage img) {
		YCbCrImage ycbcr = new YCbCrImage(new RGBImage(img));
		this.width = ycbcr.getWidth();
		this.height = ycbcr.getHeight();
		this.Y = ycbcr.getY();
		this.Cb = ycbcr.getCb();
		this.Cr = ycbcr.getCr();
	}

	public YCbCrImage(File imgFile) throws IOException {
		this(ImageIO.read(imgFile));
	}

	public YCbCrImage(URL imgURL) throws IOException {
		this(ImageIO.read(imgURL));
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	public GreyscaleImage getY() {
		return this.Y;
	}

	public GreyscaleImage getCb() {
		return this.Cb;
	}

	public GreyscaleImage getCr() {
		return this.Cr;
	}

	@Override
	public YCbCrImage deepClone() {
		return new YCbCrImage(Y.deepClone(), Cb.deepClone(), Cr.deepClone());
	}

	@Override
	public YCbCrImage resizeNearest(int width, int height) {
		return new YCbCrImage(Y.resizeNearest(width, height), Cb.resizeNearest(width, height),
				Cr.resizeNearest(width, height));
	}

	@Override
	public YCbCrImage rescaleNearest(float widthFactor, float heightFactor) {
		return new YCbCrImage(Y.rescaleNearest(widthFactor, heightFactor), Cb.rescaleNearest(widthFactor, heightFactor),
				Cr.rescaleNearest(widthFactor, heightFactor));
	}

	@Override
	public YCbCrImage resizeBilinear(int width, int height) {
		return new YCbCrImage(Y.resizeBilinear(width, height), Cb.resizeBilinear(width, height),
				Cr.resizeBilinear(width, height));
	}

	@Override
	public YCbCrImage rescaleBilinear(float widthFactor, float heightFactor) {
		return new YCbCrImage(Y.rescaleBilinear(widthFactor, heightFactor),
				Cb.rescaleBilinear(widthFactor, heightFactor), Cr.rescaleBilinear(widthFactor, heightFactor));
	}

	@Override
	public BufferedImage toBufferedImage() {
		return this.toRGB().toBufferedImage();
	}

	// Returns the average of the inverse conversion to R, G, B.
	@Override
	public GreyscaleImage toGreyscale() {
		return this.toRGB().toGreyscale();
	}

	// Inverse conversion
	@Override
	public RGBImage toRGB() {
		// Only call the function once, save a reference to avoid function overhead.
		// This is not copying the array object.
		byte[] luminance = Y.getPixels();
		byte[] chrominanceBlue = Cb.getPixels();
		byte[] chrominanceRed = Cr.getPixels();

		byte[] r = new byte[luminance.length];
		byte[] g = new byte[luminance.length];
		byte[] b = new byte[luminance.length];

		// @nof
		int j, k, l;
		for (int i = 0; i < luminance.length; i++) {
			int y = luminance[i] & 0xff;
			int cb = chrominanceBlue[i] & 0xff;
			int cr = chrominanceRed[i] & 0xff;
			
			j = ((int) (
					298.082f * (y - 16) + 408.583 *
					(cr - 128))
					) >> 8;

			k = ((int)(
					298.082 * (y - 16) + -100.291 *
					(cb - 128) + -208.120 *
					(cr - 128))
					) >> 8;

			l = ((int) 
					(298.082 * (y - 16) + 516.411 *
					(cb - 128))
					) >> 8;
			
			// Validate arrays
			if (j > 255) {j = 255;} else if (j < 0) {j = 0;}
			if (k > 255) {k = 255;} else if (k < 0) {k = 0;}
			if (l > 255) {l = 255;} else if (l < 0) {l = 0;}
			
			r[i] = (byte) j;
			g[i] = (byte) k;
			b[i] = (byte) l;
			
		}
		// @dof

		return new RGBImage(r, g, b, this.width, this.height);
	}

	// Inverse conversion plus new alpha channel
	@Override
	public RGBAImage toRGBA() {
		byte[] alpha = new byte[this.width * this.height];
		for (int i = 0; i < alpha.length; i++) {
			alpha[i] = (byte) 255;
		}
		return new RGBAImage(this.toRGB(), alpha);
	}

	@Override
	public YCbCrImage flipHorizontal() {
		return new YCbCrImage(Y.flipHorizontal(), Cb.flipHorizontal(), Cr.flipHorizontal());
	}

	@Override
	public YCbCrImage flipVertical() {
		return new YCbCrImage(Y.flipVertical(), Cb.flipVertical(), Cr.flipVertical());
	}

	@Override
	public YCbCrImage rotate90CW() {
		return new YCbCrImage(Y.rotate90CW(), Cb.rotate90CW(), Cr.rotate90CW());
	}

	@Override
	public YCbCrImage rotate90CCW() {
		return new YCbCrImage(Y.rotate90CCW(), Cb.rotate90CCW(), Cr.rotate90CCW());
	}

	@Override
	public YCbCrImage rotate180() {
		return new YCbCrImage(Y.rotate180(), Cb.rotate180(), Cr.rotate180());
	}

}
