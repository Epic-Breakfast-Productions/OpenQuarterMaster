package tech.ebp.oqm.baseStation.model.rest.tree.storageBlock;

import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.baseStation.model.rest.tree.ParentedMainObjectTree;
import tech.ebp.oqm.baseStation.model.rest.tree.storageBlock.StorageBlockTreeNode;

public class StorageBlockTree extends ParentedMainObjectTree<StorageBlock, StorageBlockTreeNode> {
	
	@Override
	protected StorageBlockTreeNode newNode(StorageBlock object) {
		return new StorageBlockTreeNode(object);
	}
}
