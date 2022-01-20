package com.ebp.openQuarterMaster.lib.core.storage.storageBlock.tree;

import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.*;

@NoArgsConstructor
public class StorageBlockTree {
    @Getter
    private final Collection<StorageBlockTreeNode> rootNodes = new ArrayList<>();
    @Getter
    private final Map<ObjectId, StorageBlockTreeNode> nodeMap = new HashMap<>();
    private final Collection<StorageBlockTreeNode> orphans = new ArrayList<>();


    public StorageBlockTree add(StorageBlock block) {
        if (nodeMap.containsKey(block.getId())) {
            return this;
        }

        StorageBlockTreeNode newNode = new StorageBlockTreeNode(block);

        this.nodeMap.put(block.getId(), newNode);

        if (block.hasParent()) {
            if (nodeMap.containsKey(newNode.getParentId())) {
                nodeMap.get(newNode.getParentId()).addChild(newNode);
            } else {
                orphans.add(newNode);
            }
        } else {
            rootNodes.add(newNode);
        }

        if (this.hasOrphans()) {
            Iterator<StorageBlockTreeNode> it = this.orphans.iterator();
            while (it.hasNext()) {
                StorageBlockTreeNode cur = it.next();

                if (nodeMap.containsKey(cur.getParentId())) {
                    it.remove();
                    nodeMap.get(cur.getParentId()).addChild(cur);
                }
            }
        }

        return this;
    }

    public StorageBlockTree add(Collection<StorageBlock> blocks) {
        for (StorageBlock curBlock : blocks) {
            this.add(curBlock);
        }
        return this;
    }

    public StorageBlockTree add(StorageBlock... blocks) {
        this.add(List.of(blocks));
        return this;
    }

    public boolean hasOrphans() {
        return !orphans.isEmpty();
    }

//    public Object generateImage(){
//        TreeLayout<TextInBox>
//
//
//        return null;
//    }


}