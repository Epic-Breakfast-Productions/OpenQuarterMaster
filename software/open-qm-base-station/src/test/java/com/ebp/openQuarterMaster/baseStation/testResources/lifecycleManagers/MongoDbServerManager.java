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
	
	private MongoDBContainer MONGO_EXE = null;
	
	@Override
	public Map<String, String> start() {
		if (MONGO_EXE == null || !MONGO_EXE.isRunning()) {
			StopWatch sw = StopWatch.createStarted();
			MONGO_EXE = new MongoDBContainer(IMAGE_NAME);
			
			MONGO_EXE.start();
			sw.stop();
			log.info("Started Test Mongo in {} at: {}", sw, MONGO_EXE.getReplicaSetUrl());
		} else {
			log.info("Mongo already started.");
		}
		
		return Map.of(
			"quarkus.mongodb.connection-string", MONGO_EXE.getReplicaSetUrl()
		);
	}
	
	@Override
	public void stop() {
		if (MONGO_EXE == null) {
			log.warn("Mongo was not started.");
			return;
		}
		MONGO_EXE.stop();
		MONGO_EXE = null;
	}
}
