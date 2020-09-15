package hashstore;

import java.util.Collection;
import java.util.List;

import hash.ImageHash;
import hash.MatchMode;
import hashstore.vptree.VPTree;
import pipeline.dedup.HashMatch;

public class VPHashStore extends VPTree<ImageHash> implements HashStore {

	private static final long serialVersionUID = 3903928230022343968L;

	@Override
	public void store(ImageHash hash) throws UnsupportedOperationException { this.add(hash); }

	@Override
	public void storeAll(Collection<? extends ImageHash> hashes) { this.addAll(hashes); }

	@Override
	public List<HashMatch> findMatches(MatchMode mode) { return null; }
	
}
