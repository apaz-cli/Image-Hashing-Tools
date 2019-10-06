# Image-Hashing-Tools
A general purpose framework for finding near-duplicate images, which provides an image library for fast pixel comparisons, along with extensible implementations and tools for image hashing and attacking image hashes, including simulating jpg/jpeg compression.

## Supported Hash Algorithms (Not all yet implemented)
Average Hash (aHash)

Difference Hash (dHash)

Perceptual Hash (pHash)

RGB Histogram Hash

Greyscale Histogram Hash

Gradient Hash (My own original algorithm)


## Supported Attacks (Not all yet implemented)
JPEG

Horizontal Flip

Random Noise

Gaussian Noise

Subimage Insertion




## Example Usage
```Java
IHashAlgorithm h = HashFactory.AVERAGE_HASH;
ImageHash hash1 = HashFactory.hash(img1, h);
ImageHash hash2 = HashFactory.hash(img2, h);
boolean matchesNormal = h.matches(img1, img2);
boolean matchesStrict = h.matches(img1, img2, MatchMode.STRICT);
```


