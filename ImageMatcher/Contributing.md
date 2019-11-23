#Please create a new branch for any changes. When you're done, start a pull request.

#How to add a new Hash Algorithm:
1. Create a new class, implement IHashAlgorithm.
2. Create a method `hash(BufferedImage img)`, which will convert the `BufferedImage` to the `IImage` implementation of your choice and `return hash(IImage img)`.
3. Decide what it takes for two hashes to match with various degrees of confidence, and write the `matches()` method. The method must account for the different `MatchModes` `SLOPPY, NORMAL, STRICT, EXACT`.
4. Write hash algorithm itself, returning `new ImageHash(hash, this.getHashName())`.
5. Add two methods to `HashFactory` for your hash, one taking `IImage<?> img`, and the other taking `BufferedImage img`, both containing only the line: `return new NewAlgorithm().hash(img);`


#How to create a variation on a Hash Algorithm:
Let's say that you want to create a variation on an algorithm. Suppose that instead of a 64 bit `AverageHash` as is default, you want to write a variable bit length version.

There must be a default version of the hash, as specified by hash(). However, you can overload hash() however you want. Remember to add another method that calls your new version of the hash with a BufferedImage. This is enforced by the interface for the default hash, but not necessarily for variations. Suppose your overloaded hash method has the signature `hash(IImage<?> img, int length)`. To use a nondefault version of the hash, do the following:
	
	BufferedImage img = ImageUtils.openImage(new URL("https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png"))
	
	// You cannot use the interface type IHashAlgortithm
	AverageHash aHash = HashFactory.AVERAGE_HASH();
	// or use new AverageHash()
	
	ImageHash h = aHash.hash(img, 256);
	
If you wish, you can also update the `Matcher` with the variation on the algorithm. However, do note that the `Matcher` is only intended for general-purpose solutions. For more specific applications, write the code elsewhere instead, closer to where it's being used. For the example of extending the functionality of `AverageHash` to a certain length however, updating the Matcher seems totally okay.

It is very important that the `matches()` method treats your variation on the algorithm differently than the default. So, whenever your new `hash()` method returns an `ImageHash`, make sure that it has a name describing the variation. In this example, instead of using `this.getHashName()`, consider setting the name to something like `this.getHashName() + length` instead.



#How to add a new Image Type:
1. Create a new class, implement IImage<NewImage>. 
2. By virtue of implementing IImage, you've already ensured that you can convert to any of the previously existing types. You've also ensured that you can But, to ensure that those existing types can convert to your type, you're going to have to add a method to IImage, and implement it for all existing types:

	abstract public NewImage toNewtype();

