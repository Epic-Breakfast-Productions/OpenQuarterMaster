package tech.ebp.oqm.baseStation.model.rest.tree.storageBlock;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.baseStation.model.rest.tree.ParentedMainObjectTreeNode;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class StorageBlockTreeNode extends ParentedMainObjectTreeNode<StorageBlock> {
	
	private final String blockLabel;
	private final String blockLocation;
	private final ObjectId firstImageId;
	
	public StorageBlockTreeNode(StorageBlock block) {
		super(block);
		
		this.blockLabel = block.getLabelText();
		this.blockLocation = block.getLocation();
		this.firstImageId = (block.getImageIds().isEmpty() ? null : block.getImageIds().get(0));
	}
}
