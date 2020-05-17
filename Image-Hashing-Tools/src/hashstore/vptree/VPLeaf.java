package hashstore.vptree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class VPLeaf<T extends MetricComparable<? extends T>> implements VantagePoint<T>, Serializable {

	private static final long serialVersionUID = -6417470140587273877L;

	// Class is not public, so these are not actually visible
	int leafNumber;
	List<T> data;

	VPLeaf(int leafNumber, List<T> data) {
		this.leafNumber = leafNumber;
		this.data = data;
	}

	VPLeaf(int leafNumber) {
		this.leafNumber = leafNumber;
		this.data = new ArrayList<T>();
	}

	void writeToFile(File f) throws FileNotFoundException, IOException {
		ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(f));
		s.writeObject(this);
		s.close();
	}

	static VPLeaf<?> readFromFile(File f)
			throws FileNotFoundException, IOException, ClassNotFoundException, ClassCastException {
		ObjectInputStream s = new ObjectInputStream(new FileInputStream(f));
		VPLeaf<?> leaf = (VPLeaf<?>) s.readObject();
		s.close();
		return leaf;
	}

	int size() {
		return data.size();
	}
	
	boolean addAll(Collection<? extends T> c) {
		return this.data.addAll(c);
	}
	
	boolean contains(Object o) {
		return this.data.contains(o);
	}

	@Override
	public List<T> getAllChildren() {
		return this.data;
	}

	@Override
	public List<T> getAllAndDestroy() {
		List<T> l = this.data;
		this.destroy();
		return l;
	}

	@Override
	public void destroy() {
		this.leafNumber = -1;
		this.data = null;
	}
	
	

}
