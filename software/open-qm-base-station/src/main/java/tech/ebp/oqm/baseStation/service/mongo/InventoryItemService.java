package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.InventoryItemSearch;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingOptions;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StoredType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.regex;

@Traced
@Slf4j
@ApplicationScoped
public class InventoryItemService extends MongoHistoriedService<InventoryItem, InventoryItemSearch> {
	
	InventoryItemService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	InventoryItemService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			InventoryItem.class,
			false
		);
	}
	
	@Override
	public void ensureObjectValid(boolean newObject, InventoryItem newOrChangedObject) {
		super.ensureObjectValid(newObject, newOrChangedObject);
		//TODO:: name not existant
	}
	
	@Deprecated
	public SearchResult<InventoryItem> search(
		String name,
		List<String> keywords,
		StoredType storedType,
		Bson sort,
		PagingOptions pagingOptions
	) {
		log.info(
			"Searching for items with: name=\"{}\", keywords={}, storedType=\"{}\"",
			name,
			keywords,
			storedType
		);
		List<Bson> filters = new ArrayList<>();
		if (name != null && !name.isBlank()) {
			filters.add(regex("name", SearchUtils.getSearchTermPattern(name)));
		}
		//TODO:: keywords, storedType
		
		Bson filter = (filters.isEmpty() ? null : and(filters));
		
		List<InventoryItem> list = this.list(
			filter,
			sort,
			pagingOptions
		);
		
		return new SearchResult<>(
			list,
			this.count(filter),
			!filters.isEmpty()
		);
	}
	
	public <T> InventoryItem<T> add(ObjectId itemId, ObjectId storageBlockId, T toAdd) {
		InventoryItem item = this.get(itemId);
		
		try {
			item.add(storageBlockId, toAdd, true);
		} catch(ClassCastException e) {
			//not given proper stored type
			//TODO:: custom exception
			throw e;
		}
		
		return item;
	}
	
	public <T> InventoryItem<T> subtract(ObjectId itemId, ObjectId storageBlockId, T toSubtract) {
		InventoryItem item = this.get(itemId);
		
		try {
			item.subtract(storageBlockId, toSubtract);
		} catch(ClassCastException e) {
			//not given proper stored type
			//TODO:: custom exception
			throw e;
		}
		
		return item;
	}
	
	public <T> InventoryItem<T> transfer(
		ObjectId itemId,
		ObjectId storageBlockIdFrom,
		ObjectId storageBlockIdTo,
		T toTransfer
	) {
		InventoryItem item = this.get(itemId);
		
		try {
			item.transfer(storageBlockIdFrom, storageBlockIdTo, toTransfer);
		} catch(ClassCastException e) {
			//not given proper stored type
			//TODO:: custom exception
			throw e;
		}
		
		return item;
	}
	
}
