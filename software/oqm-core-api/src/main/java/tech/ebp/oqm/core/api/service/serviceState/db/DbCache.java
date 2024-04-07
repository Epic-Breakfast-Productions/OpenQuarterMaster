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
	
	private List<MongoDatabase> dbCache;
	private Map<ObjectId, MongoDatabase> idCacheMap;
	private Map<String, MongoDatabase> nameCacheMap;
	
	public DbCache(List<MongoDatabase> databases){
		this(
			Collections.unmodifiableList(databases),
			Collections.unmodifiableMap(
				databases.stream().collect(Collectors.toMap(MongoDatabase::getId, Function.identity()))
			),
			Collections.unmodifiableMap(
				databases.stream().collect(Collectors.toMap(MongoDatabase::getName, Function.identity()))
			)
		);
	}
	
	public DbCache(FindIterable<MongoDatabase> databases){
		this(databases.into(new ArrayList<>()));
	}
	
	public Optional<MongoDatabase> getFromId(ObjectId id){
		return Optional.ofNullable(this.getIdCacheMap().get(id));
	}
	
	public Optional<MongoDatabase> getFromId(String id){
		return this.getFromId(new ObjectId(id));
	}
	
	public Optional<MongoDatabase> getFromName(String name){
		return Optional.ofNullable(this.getNameCacheMap().get(name));
	}
	
	public Optional<MongoDatabase> getFromIdOrName(String nameOrId){
		Optional<MongoDatabase> output = this.getFromId(nameOrId);
		if(output.isEmpty()){
			output = this.getFromName(nameOrId);
		}
		return output;
	}
}
