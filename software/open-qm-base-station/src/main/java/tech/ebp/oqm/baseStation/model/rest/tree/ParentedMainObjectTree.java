package tech.ebp.oqm.baseStation.model.rest.tree;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.HasParent;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.rest.tree.ParentedMainObjectTreeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public abstract class ParentedMainObjectTree<T extends MainObject & HasParent, N extends ParentedMainObjectTreeNode<T>> {
	
	/**
	 * @param curParent The node we are operating on
	 * @param onlyInclude The list of object Ids to keep in the tree
	 *
	 * @return If the node given should be removed as well
	 */
	private boolean cleanupTree(N curParent, Collection<ObjectId> onlyInclude) {
		if (onlyInclude.contains(curParent.getObjectId())) {
			return false;
		}
		
		curParent.getChildren().removeIf(curNode->cleanupTree((N) curNode, onlyInclude));
		
		return curParent.getChildren().isEmpty();
	}
	
	@Getter
	private final Collection<N> rootNodes = new ArrayList<>();
	@Getter
	private final Map<ObjectId, N> nodeMap = new HashMap<>();
	private final Collection<N> orphans = new ArrayList<>();
	
	protected abstract N newNode(T object);
	
	public ParentedMainObjectTree<T, N> add(T block) {
		if (nodeMap.containsKey(block.getId())) {
			return this;
		}
		
		N newNode = this.newNode(block);
		
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
			Iterator<N> it = this.orphans.iterator();
			while (it.hasNext()) {
				N cur = it.next();
				
				if (nodeMap.containsKey(cur.getParentId())) {
					it.remove();
					nodeMap.get(cur.getParentId()).addChild(cur);
				}
			}
		}
		
		return this;
	}
	
	public ParentedMainObjectTree<T, N> add(Collection<T> blocks) {
		for (T curBlock : blocks) {
			this.add(curBlock);
		}
		return this;
	}
	
	public ParentedMainObjectTree<T, N> add(T... blocks) {
		this.add(List.of(blocks));
		return this;
	}
	
	public ParentedMainObjectTree<T, N> add(Iterator<T> blocks) {
		while (blocks.hasNext()) {
			T cur = blocks.next();
			this.add(cur);
		}
		return this;
	}
	
	public boolean hasOrphans() {
		return !orphans.isEmpty();
	}
	
	public void cleanupTreeNodes(Collection<ObjectId> onlyInclude) {
		this.getRootNodes().removeIf(curNode->cleanupTree(curNode, onlyInclude));
	}
}