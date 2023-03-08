package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.rest.search.CategoriesSearch;
import tech.ebp.oqm.baseStation.rest.search.CustomUnitSearch;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.lib.core.object.storage.ItemCategory;
import tech.ebp.oqm.lib.core.units.CustomUnitEntry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.measure.Unit;
import java.util.List;

@Slf4j
@ApplicationScoped
public class CategoriesService extends MongoHistoriedObjectService<ItemCategory, CategoriesSearch> {
	
	CategoriesService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	CategoriesService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			ItemCategory.class,
			false
		);
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, ItemCategory newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: this
	}
	
}
