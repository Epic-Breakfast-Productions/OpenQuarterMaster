package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.HasParent;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.rest.tree.ParentedMainObjectTree;
import tech.ebp.oqm.core.api.model.rest.tree.ParentedMainObjectTreeNode;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;

import java.util.Collection;
import java.util.List;

public abstract class
HasParentObjService<T extends MainObject & HasParent, S extends SearchObject<T>, X extends CollectionStats, N extends ParentedMainObjectTreeNode<T>>
	extends MongoHistoriedObjectService<T, S, X>
{
	public HasParentObjService(String collectionName, Class<T> clazz, boolean allowNullEntityForCreate) {
		super(collectionName, clazz, allowNullEntityForCreate);
	}
	
	public HasParentObjService(Class<T> clazz, boolean allowNullEntityForCreate) {
		super(clazz, allowNullEntityForCreate);
	}
	
	public List<T> getTopParents(String oqmDbIdOrName) {
		return this.list(oqmDbIdOrName, Filters.exists("parent", false), null, null);
	}
	
	public List<T> getChildrenIn(String oqmDbIdOrName, ObjectId parentId) {
		return this.list(oqmDbIdOrName, Filters.eq("parent", parentId), null, null);
	}
	
	public List<T> getChildrenIn(String oqmDbIdOrName, String parentId) {
		return this.getChildrenIn(oqmDbIdOrName, new ObjectId(parentId));
	}
	
	protected abstract ParentedMainObjectTree<T, N> getNewTree();
	
	public ParentedMainObjectTree<T, N> getTree(String oqmDbIdOrName, Collection<ObjectId> onlyInclude) {
		ParentedMainObjectTree<T, N> output = this.getNewTree();
		
		FindIterable<T> results = getTypedCollection(oqmDbIdOrName).find();
		output.add(results.iterator());
		
		if (!onlyInclude.isEmpty()) {
			output.cleanupTreeNodes(onlyInclude);
		}
		
		return output;
	}
}
