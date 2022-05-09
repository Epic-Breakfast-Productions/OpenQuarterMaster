package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.lib.core.storage.items.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.regex;

@Traced
@Slf4j
@ApplicationScoped
public class InventoryItemService extends MongoService<InventoryItem> {
	
	InventoryItemService() {//required for DI
		super(null, null, null, null, null, false, null);
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
}
