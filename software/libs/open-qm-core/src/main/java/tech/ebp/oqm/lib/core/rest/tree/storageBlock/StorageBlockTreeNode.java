package tech.ebp.oqm.lib.core.rest.tree.storageBlock;

import lombok.ToString;
import tech.ebp.oqm.lib.core.object.HasParent;
import tech.ebp.oqm.lib.core.object.MainObject;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.rest.tree.ParentedMainObjectTreeNode;

import java.util.ArrayList;
import java.util.Collection;

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
