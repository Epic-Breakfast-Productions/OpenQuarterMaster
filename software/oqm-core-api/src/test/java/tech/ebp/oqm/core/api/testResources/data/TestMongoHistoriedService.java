package tech.ebp.oqm.core.api.testResources.data;

import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;

import jakarta.enterprise.context.ApplicationScoped;

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
	
	@Override
	public int getCurrentSchemaVersion() {
		return 1;
	}
}
