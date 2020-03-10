package hashstore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import hash.IHashAlgorithm;

// TODO Implement hash stores
public abstract class BaseHashStore implements HashStore {

	// Anatomy of Hash Store:
	// Root Folder
	// -Serialized Algorithm
	// -Buckets (Responsibility of individual hash stores to manage)

	File root;

	@Override
	public File getRoot() {
		return this.root;
	}

	// Validates root and writes algorithm to file.
	protected void makeRoot(File root, IHashAlgorithm alg) throws IOException {
		if (!root.exists()) {
			if (!root.mkdirs()) {
				throw new IllegalArgumentException("");
			}
		}

		if (!root.isDirectory()) {
			String err = "Error on root: " + root + ".\nThis file does not exist or is not a directory.";
			throw new IllegalArgumentException(
					root.exists() ? err : err + " Folders leading up to this path may have been created.");
		}

		File algFile = new File(root.getPath() + File.separator + "alg.dat");
		if (!algFile.exists()) {
			if (!algFile.createNewFile()) {
				throw new IllegalArgumentException("There was an issue creating the algorithm file.");
			}
		}

		BufferedReader fr = new BufferedReader(new FileReader(algFile));
		String content = fr.readLine().trim();
		String expected = alg.serialize();
		fr.close();

		if (content == null || content == "") {
			BufferedWriter fw = new BufferedWriter(new FileWriter(algFile));
			fw.write(expected);
			fw.close();
		} else if (!content.equals(expected)) {
			throw new IllegalArgumentException("This root is already being used for some other HashStore.");
		}

		this.root = root;
	}

	// Loads existing root.
	// Used to open a root that already exists
	protected IHashAlgorithm parseRoot(File root) throws IOException {
		if (!root.isDirectory()) {
			throw new IllegalArgumentException(
					"Error on root: " + root + "\nThis File object does not exist or is not a directory.");
		}

		if (!root.canRead()) {
			throw new IllegalArgumentException("Cannot read from root.");
		} else if (!root.canWrite()) {
			throw new IllegalArgumentException("Cannot read write to root.");
		}

		this.root = root;

		File algFile = new File(root.getPath() + File.separator + "alg.dat");

		if (algFile.exists()) {
			BufferedReader fw = new BufferedReader(new FileReader(algFile));
			String content = fw.readLine().trim();
			fw.close();

			if (content == null) {
				throw new IllegalArgumentException("The alg.dat file was found, but was empty.");
			}

			String[] split = content.split("\\|");
			if (split.length != 2) {
				throw new IllegalArgumentException("The alg.dat file was found, but is malformed.");
			}

			// Create a base instance that knows how to deserialize itself
			IHashAlgorithm alg = null;
			try {
				alg = (IHashAlgorithm) Class.forName(split[0].trim()).newInstance();
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(
						"The alg.dat file was found and is well-formed, but the algorithm class cannot be found.");
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalArgumentException(
						"The class withing the alg.dat file was found, but a new instance could not be created.");
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(
						"The alg.dat file was found and is well-formed, and the supposed algorithm class was found, "
								+ "but could not be cast to an IHashAlgorithm.");
			}

			return alg.deserialize(split[1]);
		} else {
			throw new IllegalArgumentException("The alg.dat file could not be found.");
		}

	}

}
