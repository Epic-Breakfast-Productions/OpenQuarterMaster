package com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

public class MongoDbServerManager implements QuarkusTestResourceLifecycleManager {
	
	@Override
	public Map<String, String> start() {
		return null;
	}
	
	@Override
	public void stop() {
	
	}
}
