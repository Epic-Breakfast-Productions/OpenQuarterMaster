package tech.ebp.oqm.core.api.testResources.lifecycleManagers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.testcontainers.Testcontainers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * https://www.testcontainers.org/features/networking/
 */
@Slf4j
public class TestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {
	
	public static final String EXTERNAL_AUTH_ARG = "externalAuth";
	public static final String UI_TEST_ARG = "uiTest";
	public static final String INT_TEST_ARG = "intTest";
	
	
	//@Rule //TODO:: play with this in the test classes
	/**
	 * https://www.testcontainers.org/modules/webdriver_containers/
	 */
	public static final SeleniumGridServerManager BROWSER_CONTAINER = new SeleniumGridServerManager();
	private static final JaegerServerManager JAEGER_SERVER_MANAGER = new JaegerServerManager();
	
	private static final Collection<QuarkusTestResourceLifecycleManager> managersAsList = new ArrayList<>(){{
		add(BROWSER_CONTAINER);
		add(JAEGER_SERVER_MANAGER);
	}};
	
	static {
		Testcontainers.exposeHostPorts(8081, 8085);
	}
	
	public static WebDriver getWebDriver() {
		return BROWSER_CONTAINER.getDriver();
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
