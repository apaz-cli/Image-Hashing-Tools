# Image-Hashing-Tools
A general purpose framework for finding near-duplicate images, which provides an image library for fast pixel comparisons, along with extensible implementations and tools for image hashing and attacking image hashes, including simulating jpg/jpeg compression.

## Supported Hash Algorithms (WIP)
### Average Hash (aHash)
Inspiration: [Hacker Factor](http://www.hackerfactor.com/blog/?/archives/529-Kind-of-Like-That.html)

Description:

### Difference Hash (dHash)
Inspiration: [Hacker Factor](http://www.hackerfactor.com/blog/?/archives/529-Kind-of-Like-That.html)

Description:

### Perceptual Hash (pHash)
Inspiration: [Hacker Factor](http://hackerfactor.com/blog/index.php%3F/archives/432-Looks-Like-It.html) [PHash.org](https://www.phash.org/)

Description:

### RGB Histogram Hash
Inspiration: ["Image Hashing Based on Color Histogram" by Bian Yang, Fan Gu and Xiamu Niu](http://manu35.magtech.com.cn/Jwk_ics/CN/abstract/abstract1269.shtml)

Description:

### Greyscale Histogram Hash
Inspiration: ["Image Hashing Based on Color Histogram" by Bian Yang, Fan Gu and Xiamu Niu]

Description: I figured that if an RGB histogram could be used to match images, then a Greyscale Implementation would also be useful for Greyscale Images.

### Gradient Hash (My own original algorithm)
Inspiration: No papers to cite

Description:

### Block Hash (blockHash)

Inspiration: ["Block Mean Value Based Image Perceptual Hashing by Bian Yang, Fan Gu and Xiamu Niu"](https://ieeexplore.ieee.org/document/4041692)

Description:


## Supported Attacks (Not all yet implemented)
JPEG Compression Simulation

Flip L/R

Random Noise

Gaussian Noise

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


