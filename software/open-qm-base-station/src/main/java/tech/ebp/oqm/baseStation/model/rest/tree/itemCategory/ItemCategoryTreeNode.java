package tech.ebp.oqm.baseStation.model.rest.tree.itemCategory;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.storage.ItemCategory;
import tech.ebp.oqm.baseStation.model.rest.tree.ParentedMainObjectTreeNode;

import java.awt.*;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class ItemCategoryTreeNode extends ParentedMainObjectTreeNode<ItemCategory> {
	
	private final String catName;
	private final Color color;
	private final Color textColor;
	private final ObjectId firstImageId;
	
	public ItemCategoryTreeNode(ItemCategory itemCategory) {
		super(itemCategory);
		
		this.catName = itemCategory.getName();
		this.color = itemCategory.getColor();
		this.textColor = itemCategory.getTextColor();
		this.firstImageId = (itemCategory.getImageIds().isEmpty() ? null : itemCategory.getImageIds().get(0));
	}
}
