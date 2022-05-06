package com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers;

import io.jaegertracing.testcontainers.JaegerAllInOne;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Map;

@Slf4j
public class JaegerServerManager implements QuarkusTestResourceLifecycleManager {
	
	public static final String JAEGER_IMAGE_TAG = "jaegertracing/all-in-one:latest";
	
	private JaegerAllInOne JAEGER_CONTAINER = null;
	
	@Override
	public Map<String, String> start() {
		if (JAEGER_CONTAINER == null || !JAEGER_CONTAINER.isRunning()) {
			org.apache.commons.lang3.time.StopWatch sw = StopWatch.createStarted();
			// https://hub.docker.com/r/jaegertracing/all-in-one/tags
			JAEGER_CONTAINER = new JaegerAllInOne(JAEGER_IMAGE_TAG);
			
			JAEGER_CONTAINER.start();
			sw.stop();
			log.info("Started Test Jaeger in {} at: {}", sw, JAEGER_CONTAINER.getQueryPort());
		} else {
			log.info("Jaeger already started.");
		}
		
		return Map.of(
			"quarkus.jaeger.endpoint",
			"http://" + JAEGER_CONTAINER.getHost() + ":" + JAEGER_CONTAINER.getCollectorThriftPort() + "/api/traces"
		);
	}
	
	@Override
	public void stop() {
		if (JAEGER_CONTAINER == null) {
			log.warn("Jaeger was not started.");
			return;
		}
		JAEGER_CONTAINER.close();
		JAEGER_CONTAINER = null;
	}
	
	@Override
	public void init(Map<String, String> initArgs) {
		QuarkusTestResourceLifecycleManager.super.init(initArgs);
	}
}
