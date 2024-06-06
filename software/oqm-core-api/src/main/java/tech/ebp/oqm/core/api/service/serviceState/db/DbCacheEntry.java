package tech.ebp.oqm.core.api.service.serviceState.db;

import com.mongodb.client.MongoDatabase;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;

@AllArgsConstructor
@Data
public class DbCacheEntry {
	private final OqmMongoDatabase oqmMongoDatabase;
	private final MongoDatabase mongoDatabase;
	
	public ObjectId getDbId(){
		return this.getOqmMongoDatabase().getId();
	}
	public String getDbName(){
		return this.getOqmMongoDatabase().getName();
	}
}
