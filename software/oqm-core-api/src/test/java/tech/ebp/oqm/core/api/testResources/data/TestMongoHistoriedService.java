package tech.ebp.oqm.core.api.testResources.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

@ApplicationScoped
public class TestMongoHistoriedService extends MongoHistoriedObjectService<TestMainObject, TestMainObjectSearch, CollectionStats> {
	
	
	public TestMongoHistoriedService() {
		super(TestMainObject.class, false);
	}
	
	@Override
	public CollectionStats getStats(String dbNameOrId) {
		return super.addBaseStats(dbNameOrId, CollectionStats.builder())
				   .build();
	}
}
