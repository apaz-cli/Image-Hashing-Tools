Considerations in the construction of image hashing algorithms and digital image fingerprinting


# Abstract



# Overview of Problem

The validation and matching of digital images is a difficult problem for these reasons:

1. Lossy compression makes cryptographic hash functions useless

2. To be used for the purpose of copyright protection, these algorithms must be robust against a small amount of malicious tampering, transmission errors, ect.

3. Generating a perceptual hash

# Current Methods

aHash

dHash

pHash

blockHash

histogram


# Remarks on optimization
Why is Hamming distance the most used way to compare hashes? Why not Euclidian or Levenshtein distance?

Probably because it's fast. This is important, but not as important as you might think.

Suppose S1 and S2 are sets of images. Consider the computational expense of comparing them. Perhaps you're S1 is a set of copyrighted illustrations, and S2 is a set of image links scraped from the web. Or, suppose S1 and S2 are just folders of images. In either case, task takes |S1| x |S2| comparisons, and |S1| + |S2| hashes. Therefore, especially for large datasets, it would logically follow that the task which must be optimized is comparison, rather than the hashing algorithm itself.

However, in most practical cases, the largest time expense will be loading the images. Either over a computer network, or just from an external hard drive or the computer's own file system. The amount of time that the actual computational work takes will be far less.

These operations are not dependent on each other if performed asynchronously. Asynchronously build a buffer of images, say up to 50 of them, and hash from the buffer at the same time, storing the hashes. Then, once the set of images to put into the buffer is depleted, begin to compare all matches asynchronously. This way, you are not bottlenecking on network/disk speeds or processor speeds. To do it naively would first bottleneck the network/disk, then bottleneck the processor.

A threadpool for building the buffer, a thread or threadpool for tearing it down and saving hashes, and a thread or threadpool to compare hashes.


# Other Choices For Comparing Hashes
Hashing vs Fingerprinting - Hashing is meant to be a fingerprint of an image, yet the only preserved sense of distance is Hamming distance. Literal fingerprints are matched very differently. The tactics used are feature extraction, and comparing pairwise similarity of the features. The sense of distance here is purely Euclidian.

Most algorithms fail against flips/rotations, making hashes invariant to such transformations very valuable. However, to implement in the matching phase would decrease the efficiency of comparing hashes, and increase the number of collisions.

Notable is that pHash fails against changes of background. Truly, a large portion of an illustration is going to be the background. Therefore, pHash, as it does what it claims to, must fail, because a different background makes an image perceptually very different.

Also, the first 

Ideally, you'd use multiple methods, but this gets very computationally expensive for larger datasets.


# My Algorithm



