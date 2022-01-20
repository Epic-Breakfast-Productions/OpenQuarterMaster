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
    private final ObjectId blockId;
    private final ObjectId parentId;

    public StorageBlockTreeNode(StorageBlock block) {
        this(
                block.getLabel(),
                block.getId(),
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
