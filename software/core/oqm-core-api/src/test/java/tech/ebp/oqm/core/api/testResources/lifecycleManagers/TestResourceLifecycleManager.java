package tech.ebp.oqm.core.api.testResources.lifecycleManagers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * https://www.testcontainers.org/features/networking/
 */
@Slf4j
public class TestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {
	//@Rule //TODO:: play with this in the test classes
	
	private static final Collection<QuarkusTestResourceLifecycleManager> managersAsList = new ArrayList<>(){{
//		add(JAEGER_SERVER_MANAGER);
	}};
	
	static {
//		Testcontainers.exposeHostPorts(8081, 8085);
	}
	
	@Override
	public void init(Map<String, String> initArgs) {
		for (QuarkusTestResourceLifecycleManager manager : managersAsList){
			manager.init(initArgs);
		}
	}
	
	@Override
	public Map<String, String> start() {
		log.info("STARTING test lifecycle resources.");
		Map<String, String> configOverride = new HashMap<>();
		
		for (QuarkusTestResourceLifecycleManager manager : managersAsList) {
			configOverride.putAll(manager.start());
		}
		
		log.info("Config overrides: {}", configOverride);
		
		return configOverride;
	}
	
	@Override
	public void stop() {
		log.info("STOPPING test lifecycle resources.");
		
		for (QuarkusTestResourceLifecycleManager manager : managersAsList) {
			manager.stop();
		}
	}
}
