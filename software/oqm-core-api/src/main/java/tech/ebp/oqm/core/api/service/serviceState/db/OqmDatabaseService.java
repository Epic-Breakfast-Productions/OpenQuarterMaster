package tech.ebp.oqm.core.api.service.serviceState.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
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
import tech.ebp.oqm.core.api.model.rest.search.OqmMongoDbSearch;
import tech.ebp.oqm.core.api.service.mongo.TopLevelMongoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service to keep track of OQM databases available, as well as create them.
 * <p>
 * Keeps a cache of these entries to ensure speed in services that utilize this.
 * <p>
 * Always ensures at least one database exists; creates one at start if none available.
 */
@Slf4j
@ApplicationScoped
public class OqmDatabaseService extends TopLevelMongoService<OqmMongoDatabase, OqmMongoDbSearch, CollectionStats> {

	@Getter
	@Setter(AccessLevel.PRIVATE)
	private DbCache databaseCache = null;

	/**
	 * The prefix to use for databases when actually connecting.
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
	public void setup() {

		if (this.getCollection().countDocuments() == 0) {
			// create a default database in case none exist
			log.info("At startup, no oqm databases existed.");

			if (!ConfigProvider.getConfig().getValue("service.dbs.ifNone.create", Boolean.class)) {
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

	/**
	 * Forces a refresh of the cache of OQM databases.
	 */
	public void refreshCache() {
		log.info("Refreshing cache of databases.");
		this.setDatabaseCache(new DbCache(this.getMongoClient(), this.getCollection().find(), this.getDatabaseCache()));
	}

	/**
	 * Gets a list of the available OQM databases.
	 *
	 * @return a list of available OQM databases.
	 */
	public List<DbCacheEntry> getDatabases() {
		return this.getDatabaseCache().getDbCache();
	}

	/**
	 * Gets a particular OQM database for use in the system.
	 * <p>
	 * Pulls from the cache. If a cache miss, refreshes the cache and tries again.
	 *
	 * @param idOrName       The id or name of the OQM database to get
	 * @param refreshedCache If the cache was just refreshed; facilitates the recursive case
	 * @return The OQM database cache entry.
	 * @throws NotFoundException If the database was not found after refreshing the cache
	 */
	private DbCacheEntry getOqmDatabase(@NonNull String idOrName, boolean refreshedCache) throws NotFoundException {
		Optional<DbCacheEntry> cacheResult = this.getDatabaseCache().getFromIdOrName(idOrName);

		if (cacheResult.isEmpty()) {
			if (!refreshedCache) {
				log.info("Cache miss! Refreshing cache.");
				this.refreshCache();
				return this.getOqmDatabase(idOrName, true);
			}
			log.info("Cache miss after refresh! Database with name or id \"{}\" not found.", idOrName);
			throw new NotFoundException("Database not found with name or id \"" + idOrName + "\"");
		}

		return cacheResult.get();
	}

	/**
	 * Gets a particular OQM database for use in the system.
	 * <p>
	 * Pulls from the cache. If a cache miss, refreshes the cache and tries again.
	 *
	 * @param idOrName The id or name of the OQM database to get
	 * @return The OQM database cache entry.
	 * @throws NotFoundException If the database was not found after refreshing the cache
	 */
	public DbCacheEntry getOqmDatabase(@NonNull String idOrName) throws NotFoundException {
		return this.getOqmDatabase(idOrName, false);
	}

	/**
	 * Adds a new OQM database.
	 * <p>
	 * Refreshes the database cache automatically.
	 *
	 * @param newDatabase The new database to add
	 * @return The new id of the new database
	 * @throws IllegalArgumentException If the new database's name already exists
	 */
	public ObjectId addOqmDatabase(@Valid OqmMongoDatabase newDatabase) throws IllegalArgumentException {
		//TODO:: add logic to validator
		boolean dbNameExists = !this.getCollection().find(Filters.eq("name", newDatabase.getName())).into(new ArrayList<>()).isEmpty();
		if (dbNameExists) {
			//TODO:: better exception
			throw new IllegalArgumentException("Database with name \"" + newDatabase.getName() + "\" already exists.");
		}
		log.info("Creating new database {}", newDatabase.getDisplayName());
		newDatabase.setId(this.getCollection().insertOne(newDatabase).getInsertedId().asObjectId().getValue());

		log.info("Created new database, id: {}", newDatabase.getId());
		this.refreshCache();
		return newDatabase.getId();
	}

	public boolean hasDatabase(OqmMongoDatabase newDb) {
		return this.getDatabaseCache().getFromNew(newDb).isPresent();
	}

	public boolean hasDatabase(String dbNameOrId) {
		return this.getDatabaseCache().getFromIdOrName(dbNameOrId).isPresent();
	}

	/**
	 * Ensures a database with the name exists.
	 * @param dbName The database name to ensure exists
	 * @return If the database was created in the method or not
	 */
	public boolean ensureDatabase(String dbName) {
		if (this.hasDatabase(dbName)) {
			return false;
		}

		this.addOqmDatabase(OqmMongoDatabase.builder()
			.name(dbName)
			.build()
		);

		return true;
	}
}
