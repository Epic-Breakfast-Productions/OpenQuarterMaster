package tech.ebp.oqm.plugin.mssController.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Optional;
import java.util.Set;

@ConfigMapping(prefix = "moduleConfig")
public interface ModuleConfig {

	@WithDefault("false")
	@WithName("autoCreateStorageBlocksInAllDbs")
	Boolean autoCreateStorageBlocksInAllDbs();
	
	@WithName("serial")
	SerialConfig serial();
	
	interface SerialConfig {
		
		@WithName("scanSerial")
		@WithDefault("true")
		boolean scanSerial();
		
		@WithName("modules")
		Set<SerialModuleConfig> modules();
		
		interface SerialModuleConfig {
			@WithName("portPath")
			String portPath();
			
			@WithName("baudRate")
			Optional<Integer> baudRate();
		}
	}
	
	
}
