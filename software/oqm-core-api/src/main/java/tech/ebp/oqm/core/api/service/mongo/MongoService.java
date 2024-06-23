package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.MongoClient;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;

@Slf4j
public class MongoService<T extends MainObject, S extends SearchObject<T>, V extends CollectionStats> {
	
	public static String getCollectionNameFromClass(Class<?> clazz) {
		return clazz.getSimpleName();
	}
	
	/**
	 * The class this collection is in charge of. Used for logging.
	 */
	@Getter
	protected final Class<T> clazz;
	
	/**
	 * The MongoDb client.
	 */
	@Inject
	@Getter(AccessLevel.PROTECTED)
	MongoClient mongoClient;
	
	public MongoService(Class<T> clazz) {
		this.clazz = clazz;
	}
}
