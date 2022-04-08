package com.ebp.openQuarterMaster.driverServer.testUtils.lifecycleManagers;

import com.ebp.openQuarterMaster.driverServer.testUtils.serial.TestSerialPortManager;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://www.testcontainers.org/features/networking/
 */
@Slf4j
public class TestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {
	private static TestSerialPortManager PORT_MANAGER = new TestSerialPortManager();
	
	public static TestSerialPortManager getPortManager(){
		return PORT_MANAGER;
	}
	
	@Override
	public void init(Map<String, String> initArgs) {
		PORT_MANAGER.init(initArgs);
	}
	
	@SneakyThrows
	@Override
	public Map<String, String> start() {
		log.info("STARTING test lifecycle resources.");
		Map<String, String> configOverride = new HashMap<>();
		
		configOverride.putAll(PORT_MANAGER.start());
		
		log.info("Config overrides: {}", configOverride);
		return configOverride;
	}
	
	@SneakyThrows
	@Override
	public void stop() {
		log.info("STOPPING test lifecycle resources.");
		
		PORT_MANAGER.stop();
	}
}
