package tech.ebp.oqm.baseStation.model.rest.tree;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.HasParent;
import tech.ebp.oqm.baseStation.model.object.MainObject;

import java.util.ArrayList;
import java.util.Collection;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class ParentedMainObjectTreeNode<T extends MainObject & HasParent> {
	
	private final Collection<ParentedMainObjectTreeNode<T>> children = new ArrayList<>();
	private final ObjectId objectId;
	private final ObjectId parentId;
	
	public ParentedMainObjectTreeNode(T object) {
		this(
			object.getId(),
			object.getParent()
		);
	}
	
	public boolean hasChildren() {
		return !this.children.isEmpty();
	}
	
	public ParentedMainObjectTreeNode<T> addChild(ParentedMainObjectTreeNode<T> newChild) {
		this.children.add(newChild);
		return this;
	}
}
