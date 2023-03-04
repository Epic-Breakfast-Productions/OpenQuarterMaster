package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.rest.search.ItemListSearch;
import tech.ebp.oqm.lib.core.object.itemList.ItemList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
