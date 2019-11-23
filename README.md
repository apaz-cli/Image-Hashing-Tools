# Image-Hashing-Tools
A general purpose framework for finding near-duplicate images, which provides an image library for fast pixel comparisons, along with extensible implementations and tools for image hashing and attacking image hashes, including simulating jpg/jpeg compression.

## Supported Colorspaces (Entire project is WIP, finished items will have a ✓)
Greyscale ✓

RGB (Red, Green, Blue) ✓

RGBA (RGB with alpha/transparency channel) ✓

YCbCr (Luminance, Chrominance toward blue, Chrominance toward red) ✓

CMYK (Cyan, Magenta, Yellow, Black)

HSI (Hue, Saturation, Intensity. A cartesian transformation of RGB colorspace)


## Supported Hash Algorithms

All examples will use Lena:


<img src="https://github.com/Aaron-Pazdera/Image-Hashing-Tools/blob/master/Examples/lena.png" width="250" height="250">


### Average Hash (aHash) ✓
Inspiration: [Hacker Factor](http://www.hackerfactor.com/blog/?/archives/529-Kind-of-Like-That.html)

Pros: Very fast

Cons: Not extremely accurate

Description:


### Difference Hash (dHash)
Inspiration: [Hacker Factor](http://www.hackerfactor.com/blog/?/archives/529-Kind-of-Like-That.html)

Pros: A good general hash. Almost as fast as aHash, and doesn't care much about changes of background.

Cons: Not robust against flips/rotations

Description:

1. Resize to 9x8

<img src="https://github.com/Aaron-Pazdera/Image-Hashing-Tools/blob/master/Examples/dHash/lena%209x8.png">

2. Reduce to Greyscale

<img src="https://github.com/Aaron-Pazdera/Image-Hashing-Tools/blob/master/Examples/dHash/lena%209x8%20greyscale.png">

3. Set the bits of the hash row by row, depending on if the pixel to the left is less than the pixel to the right. If it is, set it to 1. If not, set to 0. This results in an 8x8, laid out into a 64 bit hash.

<img src="https://github.com/Aaron-Pazdera/Image-Hashing-Tools/blob/master/Examples/dHash/lena%20dhash.png">


### Perceptual Hash (pHash)
Inspiration: [Hacker Factor](http://hackerfactor.com/blog/index.php%3F/archives/432-Looks-Like-It.html) [PHash.org](https://www.phash.org/)

Pros: 

Cons: Slow

Description:

### RGB Histogram Hash
Inspiration: ["Image Hashing Based on Color Histogram" by Bian Yang, Fan Gu and Xiamu Niu](http://manu35.magtech.com.cn/Jwk_ics/CN/abstract/abstract1269.shtml)

Pros: Invariant to flips and rotations

Cons: 

Description:

### Greyscale Histogram Hash
Inspiration: "Image Hashing Based on Color Histogram" by Bian Yang, Fan Gu and Xiamu Niu

Pros: 

Cons:

Description: I figured that if an RGB histogram could be used to match images, then a Greyscale Implementation would also be useful for Greyscale Images.


### Slice Hash (My own original algorithm)✓
Inspiration: Shower thoughts

Pros: Faster than pHash, resulting hash is extremely small

Cons: As a result of such a small hash, collisions are more common. Best used in together with other algorithms like pHash.

Description:


### Block Mean Value Hash (blockHash)

Inspiration: ["Block Mean Value Based Image Perceptual Hashing by Bian Yang, Fan Gu and Xiamu Niu"](https://ieeexplore.ieee.org/document/4041692)

Pros:

Cons:

Description:


## Supported Attacks (Not all yet implemented)
JPEG Compression Simulation

Flip Vertical L/R

Random Noise

Gaussian Noise ✓

Subimage Insertion

## Example Usage

Create and match two hashes:
```Java
IHashAlgorithm h = HashFactory.AVERAGE_HASH;
ImageHash hash1 = HashFactory.hash(img1, h);
ImageHash hash2 = HashFactory.hash(img2, h);
boolean matchesNormal = h.matches(img1, img2);
boolean matchesStrict = h.matches(img1, img2, MatchMode.STRICT);
```


