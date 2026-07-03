package tech.ebp.oqm.plugin.mssController.testResources.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.Capabilities;
import tech.ebp.oqm.plugin.mssController.testResources.modules.serial.SerialTestModuleInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.ebp.oqm.plugin.mssController.model.utils.JacksonUtils.OBJECT_MAPPER;

@Slf4j
public class TestModuleResource implements QuarkusTestResourceLifecycleManager {
	public static final String NUM_SERIAL_MODULE_RES_NAME = "serialModules";
	public static final String NUM_NET_MODULE_RES_NAME = "netModules";


	private int numSerialModules = 0;
	private int numNetModules = 0;

	private List<TestModule> modules;


	@Override
	public void init(Map<String, String> initArgs) {
		this.numSerialModules = Integer.parseInt(initArgs.getOrDefault(NUM_SERIAL_MODULE_RES_NAME, "0"));
		this.numNetModules = Integer.parseInt(initArgs.getOrDefault(NUM_NET_MODULE_RES_NAME, "0"));
	}

	@Override
	public Map<String, String> start() {
		log.info("Starting TestModuleResource.");
		this.modules = new ArrayList<>(this.numSerialModules + this.numNetModules);

		Map<String, String> configMap = new HashMap<>();

		for(int i = 0; i < this.numSerialModules; i++) {
			SerialTestModuleInterface serialInterface;
			try {
				serialInterface = new SerialTestModuleInterface(OBJECT_MAPPER);
			} catch(IOException e) {
				throw new RuntimeException("Failed to setup serial module interface: " + e.getMessage(), e);
			}

			TestModule newModule = null;
			try {
				newModule = new TestModule(
					64,
					Capabilities.builder().blockLights(true).blockLightBrightness(true).blockLightColor(true).build(),
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
		for(TestModule module : this.modules) {
			try{
				module.close();
			} catch(Exception e) {
				log.error("Failed to close module {}", module.getModuleInfo().getSerialId(), e);
			}
		}
		log.info("Done stopping TestModuleResource.");
	}

}
