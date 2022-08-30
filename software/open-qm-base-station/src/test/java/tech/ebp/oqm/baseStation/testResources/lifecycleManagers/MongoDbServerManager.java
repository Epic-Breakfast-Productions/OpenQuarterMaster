package tech.ebp.oqm.baseStation.testResources.lifecycleManagers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager.INT_TEST_ARG;

/**
 * TODO:: use dev service with 2.10. Check with int test
 */
@Slf4j
public class MongoDbServerManager implements QuarkusTestResourceLifecycleManager {
	
	// https://hub.docker.com/_/mongo?tab=tags
	private static final DockerImageName IMAGE_NAME = DockerImageName.parse("mongo:5.0.6");
	public static final String TEST_RES_CONNECTION_STRING_CONFIG_KEY = "quarkus.mongodb.connection-string-local";
	
	private MongoDBContainer mongoDBContainer = null;
	private boolean intTest = false;
	
	@Override
	public void init(Map<String, String> initArgs) {
		QuarkusTestResourceLifecycleManager.super.init(initArgs);
		
		this.intTest = Boolean.parseBoolean(initArgs.getOrDefault(INT_TEST_ARG, Boolean.toString(this.intTest)));
	}
	
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
			"quarkus.mongodb.connection-string", Utils.replaceLocalWithDockerInternalIf(this.intTest, mongoDBContainer.getReplicaSetUrl()),
			TEST_RES_CONNECTION_STRING_CONFIG_KEY, mongoDBContainer.getReplicaSetUrl()
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
