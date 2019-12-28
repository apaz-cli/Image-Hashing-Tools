#TODO 

## High Priority

Rewrite ImageSources that rely on a backing ImageSource to use nextIImage/nextBufferedImage of backing source.

Rewrite Image Downloader as ImageSource

Rewrite Safebooru Scraper as ImageSource

Validate SliceHash

Validate ImageHash to/fromString

Fix array2dToArray1d so that it doesn't need to take x, y arguments

Add subimage insertion/extraction to IImage and implement each

List a bunch of comparison tasks to do with ImageOperators

Figure out how to track Type I - II error. Do it in tasks on ImageOperators?
	
Add RGBAImage constructor to HSIImage, CMYKImage, YCbCrImage


## Backlog

Fully Implement HSIImage, CMYKImage, YCbCrImage

Add optional Integer/Long types to ImageHash

Implement DCTII/IDCTII 32x32 for pHash

Implement DCTII/IDCTII 8x8 for simulated jpg compression

Add IllegalArgumentExceptions/ArrayIndexOutOfBoundsExceptions everywhere

Write YCbCr Gamma Correction

Implement other attacks

Implement other Hashes
