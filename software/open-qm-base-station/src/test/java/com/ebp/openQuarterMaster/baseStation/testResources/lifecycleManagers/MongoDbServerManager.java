package com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@Slf4j
public class MongoDbServerManager implements QuarkusTestResourceLifecycleManager {
	// https://hub.docker.com/_/mongo?tab=tags
	private static final DockerImageName IMAGE_NAME = DockerImageName.parse("mongo:5.0.6");
	
	private MongoDBContainer mongoDBContainer = null;
	
	@Override
	public Map<String, String> start() {
		if (mongoDBContainer == null || !mongoDBContainer.isRunning()) {
			StopWatch sw = StopWatch.createStarted();
			mongoDBContainer = new MongoDBContainer(IMAGE_NAME);
			
			mongoDBContainer.start();
			sw.stop();
			log.info("Started Test Mongo in {} at: {}", sw, mongoDBContainer.getReplicaSetUrl());
		} else {
			log.info("Mongo already started.");
		}
		
		return Map.of(
			"quarkus.mongodb.connection-string", mongoDBContainer.getReplicaSetUrl()
		);
	}
	
	@Override
	public void stop() {
		if (mongoDBContainer == null) {
			log.warn("Mongo was not started.");
			return;
		}
		mongoDBContainer.stop();
		mongoDBContainer = null;
	}
}
