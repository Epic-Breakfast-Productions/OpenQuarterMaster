package tech.ebp.oqm.core.api.service.serviceState.db;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Setter(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DbCache {
	
	private static List<DbCacheEntry> getCacheEntries(MongoClient mongoClient, List<OqmMongoDatabase> databases, DbCache oldCache) {
		List<DbCacheEntry> output = new ArrayList<>();
		String databasePrefix = ConfigProvider.getConfig().getValue("quarkus.mongodb.database", String.class);
		
		for (OqmMongoDatabase oqmDb : databases) {
			DbCacheEntry newEntry;
			
			Optional<DbCacheEntry> oldEntry = (oldCache == null ? Optional.empty() : oldCache.getFromId(oqmDb.getId()));
			
			if (oldEntry.isPresent()) {
				
				//TODO:: handle name changes
				newEntry = new DbCacheEntry(
					oqmDb,
					oldEntry.get().getMongoDatabase()
				);
			} else {
				newEntry = new DbCacheEntry(
					oqmDb,
					mongoClient.getDatabase(databasePrefix + "-" + oqmDb.getName())
				);
			}
			
			output.add(newEntry);
		}
		return output;
	}
	
	private List<DbCacheEntry> dbCache;
	private Map<ObjectId, DbCacheEntry> idCacheMap;
	private Map<String, DbCacheEntry> nameCacheMap;
	
	private DbCache(List<DbCacheEntry> cacheEntries) {
		this(
			Collections.unmodifiableList(cacheEntries),
			Collections.unmodifiableMap(
				cacheEntries.stream().collect(Collectors.toMap(DbCacheEntry::getDbId, Function.identity()))
			),
			Collections.unmodifiableMap(
				cacheEntries.stream().collect(Collectors.toMap(DbCacheEntry::getDbName, Function.identity()))
			)
		);
	}
	
	public DbCache(MongoClient mongoClient, List<OqmMongoDatabase> databases, DbCache oldCache) {
		this(getCacheEntries(mongoClient, databases, oldCache));
	}
	
	public DbCache(MongoClient client, FindIterable<OqmMongoDatabase> databases, DbCache oldCache) {
		this(client, databases.into(new ArrayList<>()), oldCache);
	}
	
	public Optional<DbCacheEntry> getFromId(ObjectId id) {
		return Optional.ofNullable(this.getIdCacheMap().get(id));
	}
	
	public Optional<DbCacheEntry> getFromId(String id) {
		return this.getFromId(new ObjectId(id));
	}
	
	public Optional<DbCacheEntry> getFromName(String name) {
		return Optional.ofNullable(this.getNameCacheMap().get(name));
	}
	
	public Optional<DbCacheEntry> getFromIdOrName(String nameOrId) {
		Optional<DbCacheEntry> output = this.getFromId(nameOrId);
		if (output.isEmpty()) {
			output = this.getFromName(nameOrId);
		}
		return output;
	}
}
