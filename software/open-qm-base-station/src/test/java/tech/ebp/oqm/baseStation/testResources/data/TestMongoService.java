package tech.ebp.oqm.baseStation.testResources.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.service.mongo.MongoObjectService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TestMongoService extends MongoObjectService<TestMainObject, TestMainObjectSearch> {
	
	TestMongoService() {//required for DI
		super(null, null, null, null, null, null);
	}
	
	@Inject
	TestMongoService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			TestMainObject.class
		);
	}
}
