package hashstore.vptree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class VPReference<T extends MetricComparable<? extends T>> extends File implements VantagePoint<T>, Serializable {

	private static final long serialVersionUID = 385642549742137330L;

	VPReference(String pathname) {
		super(pathname);
	}

	@SuppressWarnings("unchecked")
	VPTree<T> load() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this));
		VPTree<T> vpt = (VPTree<T>) ois.readObject();
		vpt.setSerialized(this);
		ois.close();
		return vpt;
	}

	void save(VPTree<T> vpt) throws FileNotFoundException, IOException {
		this.delete();
		this.createNewFile();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this));
		vpt.setSerialized(this);
		oos.writeObject(vpt);
		oos.close();
	}

}
