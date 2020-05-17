package hashstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hash.ImageHash;
import hashstore.vptree.MetricComparable;
import image.PixelUtils;

public class EuclideanVector implements MetricComparable<EuclideanVector> {

	float[] data;

	EuclideanVector(float[] data) {
		PixelUtils.assertNotNull("Vector data cannot be null.", data);
		this.data = data;
	}

	float magnitude() {
		int sumSquares = 0;
		for (int i = 0; i < data.length; i++) {
			sumSquares += data[i] * data[i];
		}
		return (float) Math.sqrt(sumSquares);
	}

	float dot(EuclideanVector vec) {
		if (this.data.length == vec.getLength()) {
			throw new IllegalArgumentException("Vectors of dot product are not the same length.");
		}

		float[] vData = vec.getData();
		int sum = 0;
		for (int i = 0; i < this.data.length; i++) {
			sum += vData[i] * this.data[i];
		}
		return sum;
	}

	int getLength() {
		return this.data.length;
	}

	float[] getData() {
		return this.data;
	}

	@Override
	public double distance(EuclideanVector other) {
		int length = other.getLength();
		if (length != this.data.length) {
			throw new IllegalArgumentException(
					"Length mismatch. Tried to compute the distance between vectors of length " + this.getLength()
							+ " and " + other.getLength() + ".");
		}
		
		float sum = 0, diff;
		float[] od = other.getData();
		for (int i = 0; i < length; i++) {
			diff = this.data[i] - od[i];
			sum += diff*diff;
		}
		return Math.sqrt(sum);
	}

	// Where, for example, a 3d hyperplane is given in the form ax + by + cz - d = 0
	// EuclideanVector solutions to this describe the hyperplane. The solutions to
	// the hyperplane will be 1 dim less than this point, but the equation
	// describing the hyperplane will be one more.
	// https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_plane
	float distToHyperplane(EuclideanVector hyperplane) {
		if (this.data.length + 1 != hyperplane.getLength()) {
			throw new IllegalArgumentException(
					"The hyperplane must be one dimension larger than this vector. Expected: " + (this.data.length + 1)
							+ " Got: " + hyperplane.getLength());
		}

		float sol = hyperplane.getHyperplaneSolution();
		hyperplane = hyperplane.stripHyperplaneSolution();

		return (this.dot(hyperplane) - sol) / this.magnitude();
	}

	void writeHeader(ByteArrayOutputStream bos) throws IOException {
		bos.write(new byte[] { (byte) (this.data.length >> 24), (byte) (this.data.length >> 16),
				(byte) (this.data.length >> 8), (byte) (this.data.length) });
	}

	void writeToStream(ByteArrayOutputStream bos) throws IOException {
		byte[] barr = new byte[this.data.length * 4];
		int value, offset = 0;
		for (int i = 0; i < this.data.length;) {
			value = Float.floatToRawIntBits(this.data[i]);
			barr[offset++] = (byte) (value >> 24);
			barr[offset++] = (byte) (value >> 16);
			barr[offset++] = (byte) (value >> 8);
			barr[offset++] = (byte) (value);
		}
		bos.write(barr);
	}

	static void writeToStream(ByteArrayOutputStream bos, List<ImageHash> vecList) throws IOException {

	}

	static void writeToSteam(ByteArrayOutputStream bos, List<EuclideanVector> vecList) throws IOException {

	}

	List<EuclideanVector> readStream(ByteArrayInputStream bis) throws IOException {
		int len;
		{
			byte[] lenbytes = new byte[4];
			bis.read(lenbytes);
			len = lenbytes[0];
			len <<= 8;
			len &= lenbytes[1];
			len <<= 8;
			len &= lenbytes[2];
			len <<= 8;
			len &= lenbytes[3];
		}

		List<EuclideanVector> vecs = new ArrayList<>();
		{
			float[] vec = new float[len];
			byte[] four = new byte[4];
			int in;
			while (true) {
				for (int i = 0; i < len; i++) {
					if (bis.read(four) == -1) {
						return vecs;
					}
					in = four[0];
					in <<= 8;
					in &= four[1];
					in <<= 8;
					in &= four[2];
					in <<= 8;
					in &= four[3];
					vec[i] = Float.intBitsToFloat(in);
				}
				vecs.add(new EuclideanVector(vec));
			}
		}
	}

	private EuclideanVector stripHyperplaneSolution() {
		return new EuclideanVector(Arrays.copyOf(data, data.length - 1));
	}

	private float getHyperplaneSolution() {
		return data[data.length - 1];
	}

	@Override
	public String toString() {
		String arrStr = Arrays.toString(this.data);
		return new StringBuilder().append('<').append(arrStr.substring(1, arrStr.length() - 1)).append('>').toString();
	}
}
