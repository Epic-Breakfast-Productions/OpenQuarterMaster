package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.StorageBlockSearch;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbModValidationException;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.tree.StorageBlockTree;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Traced
@Slf4j
@ApplicationScoped
public class StorageBlockService extends MongoHistoriedService<StorageBlock, StorageBlockSearch> {
	
	StorageBlockService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	StorageBlockService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			StorageBlock.class,
			false
		);
	}
	
	@Override
	public void ensureObjectValid(boolean newObject, StorageBlock storageBlock, ClientSession clientSession) {
		super.ensureObjectValid(newObject, storageBlock, clientSession);
		
		Bson parentFilter = and(
			eq("label", storageBlock.getLabel()),
			eq("location", storageBlock.getLocation()),
			eq("parent", storageBlock.getParent())
		);
		
		//TODO:: remember what this does and why
		if(newObject){
			long count = this.count(clientSession, parentFilter);
			if(count > 0){
				throw new DbModValidationException("");
			}
		} else {
			List<StorageBlock> results = this.list(clientSession, parentFilter, null, null);
			
			if(!results.isEmpty()){
				if(results.size() > 1 || !results.get(0).getId().equals(storageBlock.getId())){
					throw new DbModValidationException("");
				}
			}
		}
		
		//ensure parent exists, not infinite loop
		if (storageBlock.getId() != null && storageBlock.hasParent()) {
			if(storageBlock.getId().equals(storageBlock.getParent())){
				throw new DbModValidationException("Storage block cannot be a parent to itself.");
			}
			
			//exists
			StorageBlock curParent;
			try {
				curParent = this.get(clientSession, storageBlock.getParent());
			} catch(DbNotFoundException e){
				throw new DbModValidationException("No parent exists for parent given.", e);
			}
			//no inf loop
			while (curParent.hasParent()){
				if(storageBlock.getId().equals(curParent.getParent())){
					throw new DbModValidationException("Not allowed to make parental loop.");
				}
				curParent = this.get(clientSession, curParent.getParent());
			}
			
		}
		
		{//check that parent isn't an infinite loop
		
		}
	}
	
	public List<StorageBlock> getTopParents(){
		return this.list(Filters.exists("parent", false), null, null);
	}
	
	public List<StorageBlock> getChildrenIn(ObjectId parentId){
		return this.list(Filters.eq("parent", parentId), null, null);
	}
	
	public List<StorageBlock> getChildrenIn(String parentId){
		return this.getChildrenIn(new ObjectId(parentId));
	}
	
	public StorageBlockTree getStorageBlockTree(Collection<ObjectId> onlyInclude) {
		StorageBlockTree output = new StorageBlockTree();
		
		
		FindIterable<StorageBlock> results = getCollection().find();
		output.add(results.iterator());
		
		if (!onlyInclude.isEmpty()) {
			output.cleanupStorageBlockTreeNode(onlyInclude);
		}
		
		return output;
	}
}
