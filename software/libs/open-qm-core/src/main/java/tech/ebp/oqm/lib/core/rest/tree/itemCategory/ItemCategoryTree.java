package tech.ebp.oqm.lib.core.rest.tree.itemCategory;

import tech.ebp.oqm.lib.core.object.storage.ItemCategory;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.rest.tree.ParentedMainObjectTree;

public class ItemCategoryTree extends ParentedMainObjectTree<ItemCategory, ItemCategoryTreeNode> {
	
	@Override
	protected ItemCategoryTreeNode newNode(ItemCategory object) {
		return new ItemCategoryTreeNode(object);
	}
}
