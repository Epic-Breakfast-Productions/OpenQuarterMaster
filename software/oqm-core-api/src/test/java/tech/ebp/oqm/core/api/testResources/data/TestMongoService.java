package tech.ebp.oqm.core.api.testResources.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.service.mongo.MongoObjectService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TestMongoService extends MongoObjectService<TestMainObject, TestMainObjectSearch, CollectionStats> {
	
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
	
	@Override
	public CollectionStats getStats() {
		return super.addBaseStats(CollectionStats.builder())
				   .build();
	}
}