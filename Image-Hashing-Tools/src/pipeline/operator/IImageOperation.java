package pipeline.operator;

import image.IImage;
import image.implementations.SourcedImage;

@FunctionalInterface
public interface IImageOperation {
	
	// Invokes the method provided with the backing image of the SourcedImage
	// provided. If the IImage is not a SourcedImage, then returns null. The image
	// must be the first argument of the method provided.

	// This is intended for use in handling recursive calls in implementing the
	// operate() method of IImageOperations or possibly IAttacks, because the IImage
	// may possibly be a SourcedImage.

	// In this way, you can call this method with the operate() itself as an
	// argument. It will work through recursively until all the SourcedImages are
	// unraveled, then put them back together in order.

	// 1. In your ImageOperation, check if the image is an instance of SourcedImage
	// 2. If yes, return handleSourced((SourcedImage) img, ClassName)
	// 3. If no, handle the actual logic of the operation.
	
	// In most cases, refer to this example:
	/*
	public final static IImageOperation greyscaleOperationExample = new IImageOperation() {
		@Override
		public IImage<?> operate(IImage<?> img) {
			if (img instanceof SourcedImage) {
				return this.handleSourced((SourcedImage) img, this);
			}

			// Handle actual logic of operation.
			// As an example, this method greyscales images passing through it, but persists
			// sources through it. Any SourcedImage passing through it will continue on as a
			// SourcedImage, with a backing GreyscaleImage. It works on chains of SourcedImages
			// also.
			return img.toGreyscale();
		}
	};
	*/
	
	// Otherwise, if you want to do something with sources, you're free to do so 
	// before that block, or not at all. But to persist sources through, it will
	// have to be handled somehow. 
	// You're even free to do something like accept ONLY SourcedImages and cast. 
	// An example of this would be:
	/*
	public final static IImageOperation sourcedOnlyExample = new IImageOperation() {
		@Override
		public IImage<?> operate(IImage<?> img) {
			if (!(img instanceof SourcedImage)) {
	 			throw new IllegalArgumentException("This Operation only accepts SourcedImages.");
			}
			SourcedImage s = (SourcedImage) img;
			System.out.println(s.getSource);
	    	return s;
		}
	};
	*/
	
	public default IImage<?> handleSourced(SourcedImage img, IImageOperation op) {
		return new SourcedImage(op.operate(img.unwrap()), img.getSource(), img.isURL());
	}
	
	public abstract IImage<?> operate(IImage<?> img);
}
