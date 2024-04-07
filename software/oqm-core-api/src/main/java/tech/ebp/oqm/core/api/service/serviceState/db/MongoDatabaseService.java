package tech.ebp.oqm.core.api.service.serviceState.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.mongo.MongoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class MongoDatabaseService extends MongoService<MongoDatabase, SearchObject<MongoDatabase>, CollectionStats> {
	
	@Getter
	@Setter(AccessLevel.PRIVATE)
	private DbCache databaseCache;
	
	@Inject
	public MongoDatabaseService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database
	) {
		super(objectMapper, mongoClient, database, MongoDatabase.class);
		this.refreshCache();
	}
	
	public void refreshCache(){
		log.info("Refreshing cache of databases.");
		this.setDatabaseCache(new DbCache(this.getCollection().find()));
	}
	
	public List<MongoDatabase> getDatabases(){
		return this.getDatabaseCache().getDbCache();
	}
	
	private MongoDatabase getDatabase(@NonNull String idOrName, boolean refreshedCache){
		Optional<MongoDatabase> cacheResult = this.getDatabaseCache().getFromIdOrName(idOrName);
		
		if(cacheResult.isEmpty()){
			if(!refreshedCache){
				log.info("Cache miss! Refreshing cache.");
				this.refreshCache();
				log.info("Cache miss after refresh! Database with name or id \"{}\" not found.", idOrName);
				return this.getDatabase(idOrName, true);
			}
			throw new NotFoundException("Database not found with name or id \"" + idOrName + "\"");
		}
		
		return cacheResult.get();
	}
	
	public MongoDatabase getDatabase(@NonNull String idOrName){
		return this.getDatabase(idOrName, false);
	}
	
	public ObjectId addDatabase(@NonNull String name, @Nullable Set<String> userIds){
		MongoDatabase newDatabase = new MongoDatabase(name, userIds);
		
		boolean dbNameExists = this.getCollection().find(Filters.eq("name", newDatabase.getName())).into(new ArrayList<>()).isEmpty();
		if(dbNameExists){
			//TODO:: better exception
			throw new IllegalArgumentException("Database with name \""+newDatabase.getName()+"\" already exists.");
		}
		
		return this.getCollection().insertOne(newDatabase).getInsertedId().asObjectId().getValue();
	}
	
	
	@Override
	public CollectionStats getStats() {
		//TODO
		return null;
	}
	
	@Override
	public long clear(@NonNull ClientSession session) {
		//TODO:: actually support?
		return 0;
	}
}
