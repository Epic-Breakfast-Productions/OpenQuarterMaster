package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecRegistry;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.lib.core.object.FileMainObject;

@Slf4j
public abstract class MongoFileService<T extends FileMainObject, S extends SearchObject<T>> extends MongoService<T, S> {
	
	GridFSBucket gridFSBucket = null;
	@Getter(AccessLevel.PROTECTED)
	CodecRegistry codecRegistry;
	
	public MongoFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection,
		CodecRegistry codecRegistry
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection);
		this.codecRegistry = codecRegistry;
	}
	
	protected MongoFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> clazz,
		CodecRegistry codecRegistry
	) {
		super(objectMapper, mongoClient, database, clazz);
		this.codecRegistry = codecRegistry;
	}
	
	
	protected GridFSBucket getGridFSBucket() {
		if (this.gridFSBucket == null) {
			this.gridFSBucket = GridFSBuckets.create(this.getDatabase(), this.getCollectionName());
		}
		return this.gridFSBucket;
	}
}
