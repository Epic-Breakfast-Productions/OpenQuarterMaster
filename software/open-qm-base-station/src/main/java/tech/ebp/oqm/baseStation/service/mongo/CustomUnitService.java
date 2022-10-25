package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.CustomUnitSearch;
import tech.ebp.oqm.lib.core.units.CustomUnitEntry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Traced
@Slf4j
@ApplicationScoped
public class CustomUnitService extends MongoHistoriedService<CustomUnitEntry, CustomUnitSearch> {
	
	CustomUnitService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	CustomUnitService(
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
			CustomUnitEntry.class,
			false
		);
	}
	
	@Override
	public void ensureObjectValid(boolean newObject, CustomUnitEntry newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: ensure name,symbol, tostring? not same as any in default set or held
	}
}
