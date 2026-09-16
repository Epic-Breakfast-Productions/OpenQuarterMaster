package tech.ebp.oqm.lib.core.rest.tree.storageBlock;

import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.rest.tree.ParentedMainObjectTree;

public class StorageBlockTree extends ParentedMainObjectTree<StorageBlock, StorageBlockTreeNode> {
	
	@Override
	protected StorageBlockTreeNode newNode(StorageBlock object) {
		return new StorageBlockTreeNode(object);
	}
}
