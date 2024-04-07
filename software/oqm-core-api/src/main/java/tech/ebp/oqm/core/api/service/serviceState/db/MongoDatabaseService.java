package tech.ebp.oqm.core.api.service.serviceState.db;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.Filters;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.mongo.MongoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class MongoDatabaseService extends MongoService<OqmMongoDatabase, SearchObject<OqmMongoDatabase>, CollectionStats> {
	
	@Getter
	@Setter(AccessLevel.PRIVATE)
	private DbCache databaseCache;
	
	protected MongoDatabaseService() {
		super(OqmMongoDatabase.class);
	}
	
	public void refreshCache(){
		log.info("Refreshing cache of databases.");
		this.setDatabaseCache(new DbCache(this.getCollection().find()));
	}
	
	public List<OqmMongoDatabase> getDatabases(){
		return this.getDatabaseCache().getDbCache();
	}
	
	private OqmMongoDatabase getDatabase(@NonNull String idOrName, boolean refreshedCache){
		Optional<OqmMongoDatabase> cacheResult = this.getDatabaseCache().getFromIdOrName(idOrName);
		
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
	
	public OqmMongoDatabase getDatabase(@NonNull String idOrName){
		return this.getDatabase(idOrName, false);
	}
	
	public ObjectId addDatabase(@NonNull String name, @Nullable Set<String> userIds){
		OqmMongoDatabase newDatabase = new OqmMongoDatabase(name, userIds);
		
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
