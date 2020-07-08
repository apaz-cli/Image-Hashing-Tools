# Image-Hashing-Tools
A general purpose framework for finding near-duplicate images, which provides an image library for fast pixel comparisons, along with extensible implementations and tools for benchmarking image hashing algorithms, as well as data structures for using these hashes for scalable content based image retrieval.

[![Generic badge](https://img.shields.io/badge/Java%208+-Passing-<COLOR>.svg)](https://shields.io/)


## Supported Hash Algorithms (Entire project is WIP, finished items will have a ✓)

#### Average Hash (aHash) ✓

#### Difference Hash (dHash) ✓

#### Perceptual Hash (pHash) ✓

#### Block Mean Value Hash (blockHash)

#### RGB Histogram Hash 

#### Machine-Learned Hash (WIP, will require external dependencies.)

All of these different hashing algorithms are going to have their own unique tradeoffs in terms of computation time, robustness, and fitness for the purpose of identifying different sorts of images. 

Average Hash is extremely fast, and the hashes are small, but it's not particularly robust.

Difference Hash is only slightly slower than aHash, and still not extremely robust, but generates much fewer false positives.

RGB Histogram Hash is perfectly robust against flips, rotations, resizing, and some other transforms, which makes it stand out. Many other algorithms can't do that. However the hashes take up a lot of space, and it fails completely against any sort of recoloring.

PHash, for example, has been proven to be extremely robust for real photographs, but takes longer to complete than other hashes and may be less exact for the hard pixel borders in digital illustrations.

I suggest that you learn more about these algorithms, and choose the one that's best for your use case. Papers are cited down below.


Soon I'm going to begin work on a machine learning model-based hash. My idea is that, at the same time, the model can learn both how to compress and decompress images to/from a very small latent space, and make sure that said latent space when interpreted as a vector is very close to other similar images in Euclidean space. By feeding in both normal and tampered images, hopefully it will be possible to create a network that provides a very robust hash, even against flips and rotations. I'll post updates as work is completed.


## Basic Example Usage

```Java
public static void main(String[] args) throws Exception {
	// Load two images
	IImage<?> img1 = new RGBImage(new URL("https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png"));
	IImage<?> img2 = 
		// Resize img1 to 1.1x its original width, 1.2x its height (Doesn't modify img1)
		img1.rescaleBilinear(1.1f, 1.2f)
		// Apply seperable convolution kernel with side length 3 and blur intensity 7
		.convolveWith(KernelFactory.gaussianBlurKernel(3, 5f))
		// Apply Gaussian noise filter with mean change of 3 and standard deviation 7
		.apply(new GaussianNoiseAttack(3f, 7f)).toGreyscale();
	
	// The IImage<?> type is fully interoperable with the rest of your project. There's also a constructor that 
	// takes a BufferedImage, and .toBufferedImage() can convert it back. There's also a constructor that loads a 
	// file, and a .save() method. You don't need to provide a type parameter.
	
	// Display the images on the screen to visually observe the difference
	ImageUtils.showImage(img1);
	ImageUtils.showImage(img2);
	
	// Assert that the image and the transformed image match each other
	IHashAlgorithm pHash = new PerceptualHash();
	boolean matches = pHash.matches(img1, img2);

	// This is another equivalent way to do it that allows you to choose how close of a match you want to assert
	ImageHash h1, h2;
	h1 = pHash.hash(img1);
	h2 = pHash.hash(img2);
	matches = pHash.matches(h1, h2, MatchMode.SLOPPY);
	matches = pHash.matches(h1, h2, MatchMode.NORMAL);
	matches = pHash.matches(h1, h2, MatchMode.STRICT);
	matches = pHash.matches(h1, h2, MatchMode.EXACT);
	
	// Other algorithms are supported, with more to come.
	alg = new AverageHash();
	alg = new DifferenceHash();
}
```


## Supported Operations
JPEG Compression Simulation

Flip Vertical/Horizontal ✓

Random Noise ✓

Gaussian Noise ✓

Subimage Insertion ✓

Gaussian Blur ✓

Sharpen

Laplace of Gaussian Edge Detection


## Supported Colorspaces

#### Greyscale ✓

#### RGB (Red, Green, Blue) ✓

#### RGBA (RGB with alpha/transparency channel) ✓

#### YCbCr (Luminance, Chrominance toward blue, Chrominance toward red) (Update: Conversion to/from other IImage<?> types is done, some IImage<?> methods still need to be implemented.)




## Inspiration, Citations, And Cool Papers to Read
RGBHistogramHash
["Image Hashing Based on Color Histogram" by Bian Yang, Fan Gu and Xiamu Niu](http://manu35.magtech.com.cn/Jwk_ics/CN/abstract/abstract1269.shtml)

BlockHash
["Block Mean Value Based Image Perceptual Hashing by Bian Yang, Fan Gu and Xiamu Niu"](https://ieeexplore.ieee.org/document/4041692)

AHash/PHash:
[Hacker Factor](http://hackerfactor.com/blog/index.php%3F/archives/432-Looks-Like-It.html),
[PHash.org](https://www.phash.org/)

DHash:
[Hacker Factor](http://www.hackerfactor.com/blog/?/archives/529-Kind-of-Like-That.html)

