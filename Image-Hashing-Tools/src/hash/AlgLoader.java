package hash;

import java.util.HashMap;

import utils.Pair;

// This is perhaps the most asinine code that I've ever written.
public class AlgLoader {

	public static void register(IHashAlgorithm alg) {
		singleton.registerNonStatic(alg);
	}

	public static void forget() {
		singleton = new AlgLoader();
	}

	// Never returns null, throws ClassNotFoundException if not registered.
	public static IHashAlgorithm loadAlgorithm(String name, String arguments) throws ClassNotFoundException {
		Pair<String, String> key = new Pair<String, String>(name, arguments);
		IHashAlgorithm alg = singleton.instanceMap.get(key);
		if (alg != null) return alg;
		else {
			alg = algFromName(name).fromArguments(arguments);
			singleton.instanceMap.put(key, alg); // can't replace
			return alg;
		}
	}

	// All this is awful boilerplate, but necessary to convert from non-static to
	// static.
	private AlgLoader() {
	}

	private static AlgLoader singleton = new AlgLoader();

	// Name to base instance
	private HashMap<String, IHashAlgorithm> algMap = new HashMap<>();
	// Arguments to created and then loaded instance.
	private HashMap<Pair<String, String>, IHashAlgorithm> instanceMap = new HashMap<>();

	private void registerNonStatic(IHashAlgorithm alg) {
		if (!singleton.algMap.containsKey(alg.algName())) singleton.algMap.put(alg.algName(), alg);
	}

	private static IHashAlgorithm algFromName(String name) throws ClassNotFoundException {
		IHashAlgorithm alg = singleton.algMap.get(name);
		if (alg == null) throw new ClassNotFoundException("Please register the algorithm with name: " + name
				+ " before you try to load in ImageHash of that type.");
		return alg;
	}
}
