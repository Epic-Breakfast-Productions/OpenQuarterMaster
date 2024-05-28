package tech.ebp.oqm.core.api.service.serviceState.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.rest.search.OqmMongoDbSearch;
import tech.ebp.oqm.core.api.service.mongo.TopLevelMongoService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class OqmDatabaseService extends TopLevelMongoService<OqmMongoDatabase, OqmMongoDbSearch, CollectionStats> {
	
	@Getter
	@Setter(AccessLevel.PRIVATE)
	private DbCache databaseCache = null;
	
	/**
	 * The name of the database to access
	 */
	@Getter
	@ConfigProperty(name = "quarkus.mongodb.database")
	String databasePrefix;
	
	/**
	 * The actual mongo collection.
	 */
	private MongoCollection<OqmMongoDatabase> collection = null;
	
	protected OqmDatabaseService() {
		super(OqmMongoDatabase.class);
	}
	
	@PostConstruct
	public void setup(){
		if(this.getCollection().countDocuments() == 0){
			log.info("At startup, no oqm databases existed.");
			
			if(!ConfigProvider.getConfig().getValue("service.dbs.ifNone.create", Boolean.class)){
				log.info("Configured to not create default database.");
			} else {
				log.info("Adding new database to ensure one exists.");
				this.addOqmDatabase(
					OqmMongoDatabase.builder()
						.name(ConfigProvider.getConfig().getValue("service.dbs.ifNone.name", String.class))
						.displayName(ConfigProvider.getConfig().getValue("service.dbs.ifNone.displayName", String.class))
						.description(ConfigProvider.getConfig().getValue("service.dbs.ifNone.description", String.class))
						.build()
				);
			}
		}
		this.refreshCache();
	}
	
	/**
	 * Gets the collection for this service.
	 * <p>
	 * Sets up the collection object if not initialized yet.
	 *
	 * @return The Mongo collection for this service.
	 */
	protected MongoCollection<OqmMongoDatabase> getCollection() {
		if (this.collection == null) {
			this.collection = this.getMongoDatabase().getCollection(OqmMongoDatabase.class.getSimpleName(), OqmMongoDatabase.class);
		}
		return this.collection;
	}
	
	
	public void refreshCache(){
		log.info("Refreshing cache of databases.");
		this.setDatabaseCache(new DbCache(this.getMongoClient(), this.getCollection().find(), this.getDatabaseCache()));
	}
	
	public List<DbCacheEntry> getDatabases(){
		return this.getDatabaseCache().getDbCache();
	}
	
	private DbCacheEntry getOqmDatabase(@NonNull String idOrName, boolean refreshedCache){
		Optional<DbCacheEntry> cacheResult = this.getDatabaseCache().getFromIdOrName(idOrName);
		
		if(cacheResult.isEmpty()){
			if(!refreshedCache){
				log.info("Cache miss! Refreshing cache.");
				this.refreshCache();
				return this.getOqmDatabase(idOrName, true);
			}
			log.info("Cache miss after refresh! Database with name or id \"{}\" not found.", idOrName);
			throw new NotFoundException("Database not found with name or id \"" + idOrName + "\"");
		}
		
		return cacheResult.get();
	}
	
	public DbCacheEntry getOqmDatabase(@NonNull String idOrName){
		return this.getOqmDatabase(idOrName, false);
	}
	
	public ObjectId addOqmDatabase(OqmMongoDatabase newDatabase){
		//TODO:: add logic to validator
		boolean dbNameExists = !this.getCollection().find(Filters.eq("name", newDatabase.getName())).into(new ArrayList<>()).isEmpty();
		if(dbNameExists){
			//TODO:: better exception
			throw new IllegalArgumentException("Database with name \""+newDatabase.getName()+"\" already exists.");
		}
		log.info("Creating new database {}", newDatabase.getDisplayName());
		newDatabase.setId(this.getCollection().insertOne(newDatabase).getInsertedId().asObjectId().getValue());
		
		log.info("Created new database, id: {}", newDatabase.getId());
		this.refreshCache();
		return newDatabase.getId();
	}

	public boolean hasDatabase(OqmMongoDatabase newDb){
		return this.getDatabaseCache().getFromNew(newDb).isPresent();
	}
	public boolean hasDatabase(String dbNameOrId){
		return this.getDatabaseCache().getFromIdOrName(dbNameOrId).isPresent();
	}

	public boolean ensureDatabase(String dbName){
		if(this.hasDatabase(dbName)){
			return false;
		}

		this.addOqmDatabase(OqmMongoDatabase.builder()
			.name(dbName)
			.build()
		);

		return true;
	}
}
