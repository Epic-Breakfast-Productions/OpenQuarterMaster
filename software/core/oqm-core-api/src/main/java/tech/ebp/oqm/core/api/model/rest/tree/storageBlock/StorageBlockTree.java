package tech.ebp.oqm.core.api.model.rest.tree.storageBlock;

import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.tree.ParentedMainObjectTree;

public class StorageBlockTree extends ParentedMainObjectTree<StorageBlock, StorageBlockTreeNode> {
	
	@Override
	protected StorageBlockTreeNode newNode(StorageBlock object) {
		return new StorageBlockTreeNode(object);
	}
}
