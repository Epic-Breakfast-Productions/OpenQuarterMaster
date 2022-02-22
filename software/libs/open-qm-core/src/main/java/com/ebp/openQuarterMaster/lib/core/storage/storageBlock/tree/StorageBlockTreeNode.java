package com.ebp.openQuarterMaster.lib.core.storage.storageBlock.tree;

import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collection;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class StorageBlockTreeNode {
	
	private final Collection<StorageBlockTreeNode> children = new ArrayList<>();
	private final String blockLabel;
	private final String blockLocation;
	private final ObjectId blockId;
	private final ObjectId firstImageId;
	private final ObjectId parentId;
	
	public StorageBlockTreeNode(StorageBlock block) {
		this(
			block.getLabel(),
			block.getLocation(),
			block.getId(),
			(block.getImageIds().isEmpty() ? null : block.getImageIds().get(0)),
			block.getParent()
		);
	}
	
	
	public boolean hasChildren() {
		return !this.children.isEmpty();
	}
	
	public StorageBlockTreeNode addChild(StorageBlockTreeNode newChild) {
		this.children.add(newChild);
		return this;
	}
	
}
