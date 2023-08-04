package tech.ebp.oqm.baseStation.model.rest.tree.itemCategory;

import tech.ebp.oqm.baseStation.model.object.storage.ItemCategory;
import tech.ebp.oqm.baseStation.model.rest.tree.ParentedMainObjectTree;
import tech.ebp.oqm.baseStation.model.rest.tree.itemCategory.ItemCategoryTreeNode;

public class ItemCategoryTree extends ParentedMainObjectTree<ItemCategory, ItemCategoryTreeNode> {
	
	@Override
	protected ItemCategoryTreeNode newNode(ItemCategory object) {
		return new ItemCategoryTreeNode(object);
	}
}
