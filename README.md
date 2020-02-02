# Image-Hashing-Tools
A general purpose framework for finding near-duplicate images, which provides an image library for fast pixel comparisons, along with extensible implementations and tools for image hashing and attacking image hashes, including simulating jpg/jpeg compression.


## Supported Colorspaces (Entire project is WIP, finished items will have a ✓)
#### Greyscale ✓

#### RGB (Red, Green, Blue) ✓

#### RGBA (RGB with alpha/transparency channel) ✓

#### YCbCr (Luminance, Chrominance toward blue, Chrominance toward red) 



## Supported Hash Algorithms

#### Average Hash (aHash) ✓

#### Difference Hash (dHash) ✓

#### Perceptual Hash (pHash) ✓

#### Block Mean Value Hash (blockHash)

#### RGB Histogram Hash 

All of these different hashing algorithms are going to have their own unique tradeoffs in terms of computation time, robustness, and fitness for the purpose of identifying different sorts of images. 

Average Hash is extremely fast, and the hashes are small, but it's not particularly robust.

Difference Hash is only slightly slower than aHash, and still not extremely robust, but generates much fewer false positives.

RGB Histogram Hash is perfectly robust against flips, rotations, resizing, and some other transforms, which makes it stand out. Many other algorithms can't do that. However the hashes take up a lot of space, and it fails completely against any sort of recoloring.

PHash, for example, has been proven to be extremely robust for real photographs, but takes longer to complete than other hashes and may be less exact for the hard pixel borders in digital illustrations.

I suggest that you learn more about these algorithms, and choose the one that's best for your use case. Papers are cited down below.


#### Machine-Learned Hash (WIP, will require external dependencies.)

Soon I'm going to begin work on a machine learning model-based hash. My idea is that, at the same time, the model can learn both how to compress and decompress images to/from a very small latent space, and make sure that said latent space when interpreted as a vector is very close to other similar images in Euclidean space. By feeding in both normal and tampered images, hopefully it will be possible to create a network that provides a very robust hash, even against flips and rotations. I'll post updates as work is completed.


## Supported Operations
JPEG Compression Simulation

Flip Vertical/Horizontal ✓

Random Noise ✓

Gaussian Noise ✓

Subimage Insertion ✓

Gaussian Blur ✓

Sharpen

Laplace of Gaussian Edge Detection

## Example Usage

Create images and check if they match robustly:
```Java
IImage<?> img1 = null, img2 = null;
		URL imageURL = null;
		try {
			// Download image from file/url.
			// Try copy/pasting all sorts of image files/links here.
			img1 = new RGBImage(new URL("https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png"));
			img2 = img1
				// 1.1x width, 1.2x height
				.rescaleBilinear(1.1f, 1.2f)
				// Kernel side length, blur intensity
				.convolveWith(KernelFactory.gaussianBlurKernel(7, 5f))
				// Mean, Standard Deviation
				.apply(new GaussianNoiseAttack(3f, 7f)).toGreyscale();

		} catch (IOException e) {
			System.err.println("Failed to load the image.");
			e.printStackTrace();
			System.exit(1);
		}
		
		IHashAlgorithm pHash = new PerceptualHash();
		boolean matches = pHash.matches(img1, img2);
		
		// This is another equivalent way to do it
		ImageHash h1, h2;
		h1 = pHash.hash(img1);
		h2 = pHash.hash(img2);
		matches = pHash.matches(h1, h2);
		
		// The images match, even though we did all the things above to one of them.
		System.out.println(matches ? "MATCH" : "FAILED TO MATCH");

		// Display the images
		ImageUtils.showImage(img1);
		ImageUtils.showImage(img2);
```

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

