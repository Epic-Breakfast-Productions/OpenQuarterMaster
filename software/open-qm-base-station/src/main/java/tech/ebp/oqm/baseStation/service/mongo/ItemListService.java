package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.rest.search.ItemListSearch;
import tech.ebp.oqm.baseStation.rest.search.StorageBlockSearch;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbModValidationException;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.lib.core.object.itemList.ItemList;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.tree.StorageBlockTree;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
@ApplicationScoped
public class ItemListService extends MongoHistoriedObjectService<ItemList, ItemListSearch> {
	
	ItemListService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	ItemListService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			ItemList.class,
			false
		);
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, ItemList list, ClientSession clientSession) {
		super.ensureObjectValid(newObject, list, clientSession);
		
	}
	
}
