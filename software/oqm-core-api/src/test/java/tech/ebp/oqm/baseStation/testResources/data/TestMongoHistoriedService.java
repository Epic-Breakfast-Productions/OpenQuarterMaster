package tech.ebp.oqm.baseStation.testResources.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.model.collectionStats.CollectionStats;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.ebp.oqm.baseStation.service.notification.HistoryEventNotificationService;

@ApplicationScoped
public class TestMongoHistoriedService extends MongoHistoriedObjectService<TestMainObject, TestMainObjectSearch, CollectionStats> {
	
	TestMongoHistoriedService() {//required for DI
		super(null, null, null, null, null, null, false, null, null);
	}
	
	@Inject
	TestMongoHistoriedService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database,
		HistoryEventNotificationService hens
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			TestMainObject.class,
			false,
			hens
		);
	}
	
	@Override
	public CollectionStats getStats() {
		return super.addBaseStats(CollectionStats.builder())
				   .build();
	}
}
