package tech.ebp.oqm.core.api.service.serviceState.db;

import com.mongodb.client.FindIterable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import org.bson.types.ObjectId;

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
	
	private List<OqmMongoDatabase> dbCache;
	private Map<ObjectId, OqmMongoDatabase> idCacheMap;
	private Map<String, OqmMongoDatabase> nameCacheMap;
	
	public DbCache(List<OqmMongoDatabase> databases){
		this(
			Collections.unmodifiableList(databases),
			Collections.unmodifiableMap(
				databases.stream().collect(Collectors.toMap(OqmMongoDatabase::getId, Function.identity()))
			),
			Collections.unmodifiableMap(
				databases.stream().collect(Collectors.toMap(OqmMongoDatabase::getName, Function.identity()))
			)
		);
	}
	
	public DbCache(FindIterable<OqmMongoDatabase> databases){
		this(databases.into(new ArrayList<>()));
	}
	
	public Optional<OqmMongoDatabase> getFromId(ObjectId id){
		return Optional.ofNullable(this.getIdCacheMap().get(id));
	}
	
	public Optional<OqmMongoDatabase> getFromId(String id){
		return this.getFromId(new ObjectId(id));
	}
	
	public Optional<OqmMongoDatabase> getFromName(String name){
		return Optional.ofNullable(this.getNameCacheMap().get(name));
	}
	
	public Optional<OqmMongoDatabase> getFromIdOrName(String nameOrId){
		Optional<OqmMongoDatabase> output = this.getFromId(nameOrId);
		if(output.isEmpty()){
			output = this.getFromName(nameOrId);
		}
		return output;
	}
}
