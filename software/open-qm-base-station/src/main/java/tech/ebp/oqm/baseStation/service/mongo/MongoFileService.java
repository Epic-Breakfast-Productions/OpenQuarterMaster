package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.lib.core.object.MainObject;

@Slf4j
public abstract class MongoFileService<T extends MainObject, S extends SearchObject<T>> extends MongoService<T, S> {
	
	public MongoFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection);
	}
	
	protected MongoFileService(ObjectMapper objectMapper, MongoClient mongoClient, String database, Class<T> clazz) {
		super(objectMapper, mongoClient, database, clazz);
	}
	
}
