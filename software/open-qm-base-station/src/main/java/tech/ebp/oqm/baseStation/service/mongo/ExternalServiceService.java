package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.ExternalServiceSearch;
import tech.ebp.oqm.lib.core.object.externalService.ExternalService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Traced
@Slf4j
@ApplicationScoped
public class ExternalServiceService extends MongoHistoriedService<ExternalService, ExternalServiceSearch> {
	//    private Validator validator;
	
	ExternalServiceService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	ExternalServiceService(
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
			ExternalService.class,
			true
		);
		//        this.validator = validator;
	}
	
	@Override
	public void ensureObjectValid(boolean newObject, ExternalService newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: name not existant
	}
}
