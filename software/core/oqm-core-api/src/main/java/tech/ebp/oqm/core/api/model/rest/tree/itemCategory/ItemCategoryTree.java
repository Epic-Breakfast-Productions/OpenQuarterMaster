package tech.ebp.oqm.core.api.model.rest.tree.itemCategory;

import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.rest.tree.ParentedMainObjectTree;

public class ItemCategoryTree extends ParentedMainObjectTree<ItemCategory, ItemCategoryTreeNode> {
	
	@Override
	protected ItemCategoryTreeNode newNode(ItemCategory object) {
		return new ItemCategoryTreeNode(object);
	}
}
