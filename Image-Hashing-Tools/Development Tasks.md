#TODO 


## Nouns:

Images:
GreyscaleImage, Basic color channel
SourcedImage, Any other IImage, plus where it came from
IImage, Basic Image interface.

Operations: 
IImageOperation, takes an IImage and returns an IImage 
IAttack extends IImageOperation, Applies the operation to each color channel
ConvolutionKernel extends IAttack, Applies itself to each color channel and encapsulates information about the kernel.

Pipeline:
ImageSource, represents a stream of images
ImageHasher, Consumes ImageSource, manages resulting hashes


## High Priority

With ImageHash storage in mind, Refactor/rewrite ImageHash such that it's natural to differentiate what sort of hash it is (Euclidean/Hamming) and how it's stored (# of bits)

Figure out how to track Type I - II error. Do it both in ImageOperators and in a HasherOutput/HashStore.

Fix array2dToArray1d so that it doesn't need to take x, y arguments

List a bunch of comparison tasks to do with ImageOperators


## Backlog

Fully Implement YCbCrImage

Add functionality for shifting in HSI colorspace

Add IllegalArgumentExceptions/ArrayIndexOutOfBoundsExceptions everywhere

Write YCbCr Gamma Correction

Implement other attacks

Implement other Hashes
