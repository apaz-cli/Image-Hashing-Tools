#TODO 

##Attack:
Write attacks
JPEG compression
Vertical flip


##Hash:
Implement Hashes


##Image:
Add IllegalArgumentExceptions/ArrayIndexOutOfBoundsExceptions everywhere
Validate Greyscale NearestNeighbor 
Write Greyscale Bicubic rescale
Write subimage insertion/extraction
Write YCbCr Gamma Correction


##Utils:
Implement DCTII/IDCTII 32x32 for pHash
Implement DCTII/IDCTII 8x8 for simulated jpg compression
Write JPEG compression pipeline
Test and utilize ImageUtils.to1DArray() and .to2DArray()

Here's a possible example of what that would look like.

	IHashAlgorithm h = HashFactory.AVERAGE_HASH;
	ImageHash hash1 = HashFactory.hash(img1, h);
	ImageHash hash2 = HashFactory.hash(img2, h);
	boolean matchesNormal = h.matches(img1, img2);
	boolean matchesStrict = h.matches(img1, img2, MatchMode.STRICT);
