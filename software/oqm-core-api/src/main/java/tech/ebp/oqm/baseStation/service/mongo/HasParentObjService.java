package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.collectionStats.CollectionStats;
import tech.ebp.oqm.baseStation.model.object.HasParent;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.rest.tree.ParentedMainObjectTree;
import tech.ebp.oqm.baseStation.model.rest.tree.ParentedMainObjectTreeNode;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;

import java.util.Collection;
import java.util.List;

public abstract class
	HasParentObjService<
						   T extends MainObject & HasParent,
						   S extends SearchObject<T>,
						   X extends CollectionStats,
						   N extends ParentedMainObjectTreeNode<T>
						   >
	extends MongoHistoriedObjectService<T, S, X>
{
	
	public HasParentObjService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection,
		boolean allowNullEntityForCreate,
		MongoHistoryService<T> historyService
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection, allowNullEntityForCreate, historyService);
	}
	
	protected HasParentObjService(ObjectMapper objectMapper, MongoClient mongoClient, String database, Class<T> clazz, boolean allowNullEntityForCreate) {
		super(objectMapper, mongoClient, database, clazz, allowNullEntityForCreate);
	}
	
	@WithSpan
	public List<T> getTopParents(){
		return this.list(Filters.exists("parent", false), null, null);
	}
	
	@WithSpan
	public List<T> getChildrenIn(ObjectId parentId){
		return this.list(Filters.eq("parent", parentId), null, null);
	}
	
	@WithSpan
	public List<T> getChildrenIn(String parentId){
		return this.getChildrenIn(new ObjectId(parentId));
	}
	
	protected abstract ParentedMainObjectTree<T, N> getNewTree();
	
	public ParentedMainObjectTree<T, N> getTree(Collection<ObjectId> onlyInclude){
		ParentedMainObjectTree<T, N> output = this.getNewTree();
		
		FindIterable<T> results = getCollection().find();
		output.add(results.iterator());
		
		if (!onlyInclude.isEmpty()) {
			output.cleanupTreeNodes(onlyInclude);
		}
		
		return output;
	}
}
