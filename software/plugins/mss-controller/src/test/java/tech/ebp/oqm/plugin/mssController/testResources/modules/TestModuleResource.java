package tech.ebp.oqm.plugin.mssController.testResources.modules;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.Capabilities;
import tech.ebp.oqm.plugin.mssController.service.media.ModuleStateImageService;
import tech.ebp.oqm.plugin.mssController.testResources.modules.engine.TestModuleEngine;
import tech.ebp.oqm.plugin.mssController.testResources.modules.modInterfaces.serial.SerialTestModuleInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.ebp.oqm.plugin.mssController.model.utils.JacksonUtils.OBJECT_MAPPER;

@Slf4j
public class TestModuleResource implements QuarkusTestResourceLifecycleManager {
	public static final String NUM_SERIAL_MODULE_RES_NAME = "serialModules";
	public static final String NUM_NET_MODULE_RES_NAME = "netModules";

	@Getter
	private static List<TestModule> modules;

	private int numSerialModules = 0;
	private int numNetModules = 0;




	@Override
	public void init(Map<String, String> initArgs) {
		this.numSerialModules = Integer.parseInt(initArgs.getOrDefault(NUM_SERIAL_MODULE_RES_NAME, "0"));
		this.numNetModules = Integer.parseInt(initArgs.getOrDefault(NUM_NET_MODULE_RES_NAME, "0"));
	}

	@Override
	public Map<String, String> start() {
		log.info("Starting TestModuleResource.");
		modules = new ArrayList<>(this.numSerialModules + this.numNetModules);

		TestModuleEngine.TestModuleEngineBuilder testModuleEngineBuilder = TestModuleEngine.builder();
		testModuleEngineBuilder.numBlocks(64);
		testModuleEngineBuilder.capabilities(Capabilities.builder().blockLights(true).blockLightBrightness(true).blockLightColor(true).build());

		Map<String, String> configMap = new HashMap<>();

		for(int i = 0; i < this.numSerialModules; i++) {
			TestModuleEngine engine = testModuleEngineBuilder.build();

			SerialTestModuleInterface serialInterface;
			try {
				serialInterface = new SerialTestModuleInterface(OBJECT_MAPPER, engine);
			} catch(IOException e) {
				throw new RuntimeException("Failed to setup serial module interface: " + e.getMessage(), e);
			}

			TestModule newModule;
			try {
				newModule = new TestModule(
					engine,
					serialInterface
				);
			} catch(Exception e) {
				throw new RuntimeException("Failed to setup and initialize module.", e);
			}
			this.modules.add(newModule);

			configMap.put(
				"moduleConfig.serial.modules[" + i + "].portPath",
				serialInterface.getMssConnectionPortLocation()
			);

		}


		log.info("Done starting TestModuleResource: {} / {}", this.modules, configMap);
		return configMap;
	}

	@Override
	public void stop() {
		log.info("Stopping TestModuleResource.");
		for(TestModule module : modules) {
			try{
				module.close();
			} catch(Exception e) {
				log.error("Failed to close module {}", module.getModuleInfo().getSerialId(), e);
			}
		}
		modules = null;
		log.info("Done stopping TestModuleResource.");
	}

	public static List<TestModule> getTestModules(){
		if(modules == null) {
			return Collections.emptyList();
		}
		return modules;
	}

	public static void resetModuleState(){
		for(TestModule module : getTestModules()) {
			module.resetModuleState();
		}
	}
}
