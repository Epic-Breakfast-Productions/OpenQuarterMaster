package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.rest.search.StorageBlockSearch;
import com.ebp.openQuarterMaster.baseStation.service.mongo.exception.DbModValidationException;
import com.ebp.openQuarterMaster.baseStation.service.mongo.exception.DbNotFoundException;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.tree.StorageBlockTree;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

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
	public void ensureObjectValid(boolean newObject, StorageBlock storageBlock) {
		super.ensureObjectValid(newObject, storageBlock);
		
		Bson parentFilter = and(
			eq("label", storageBlock.getLabel()),
			eq("location", storageBlock.getLocation()),
			eq("parent", storageBlock.getParent())
		);
		
		if(newObject){
			long count = this.count(parentFilter);
			if(count > 0){
				throw new DbModValidationException("");
			}
		} else {
			List<StorageBlock> results = this.list(parentFilter, null, null);
			
			if(!results.isEmpty()){
				if(results.size() > 1 || !results.get(0).getId().equals(storageBlock.getId())){
					throw new DbModValidationException("");
				}
			}
		}
		
		if (storageBlock.getParent() != null) {
			try {
				this.get(storageBlock.getParent());
			} catch(DbNotFoundException e){
				throw new DbModValidationException("No parent exists for parent given.", e);
			}
		}
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
