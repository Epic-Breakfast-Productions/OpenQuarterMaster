package tech.ebp.oqm.plugin.mssController.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@ConfigMapping(prefix = "moduleConfig")
public interface ModuleConfig {

	@WithName("recordDir")
	@WithDefault("/tmp/mssController/modules/")
	Path recordDir();

	@WithName("serial")
	SerialConfig serial();

	@WithName("net")
	NetConfig net();

	interface SerialConfig {

		ScanConfig scan();

		@WithName("modules")
		Set<SerialModuleConfig> modules();

		Timings timings();

		interface Timings {
			@WithName("rwTimeout")
			@WithDefault("0.5s")
			Duration rwTimeout();

			@WithDefault("0.1s")
			Duration commSpacing();
		}

		interface SerialModuleConfig {
			@WithName("portPath")
			String portPath();

			@WithName("baudRate")
			Optional<Integer> baudRate();
		}

		interface ScanConfig {
			@WithDefault("true")
			boolean enabled();

			@WithName("scanDir")
			@WithDefault("/dev/")//TODO:: doublecheck
			String scanDir();
		}
	}

	interface NetConfig {

		@WithName("modules")
		Set<NetModuleConfig> modules();

		interface NetModuleConfig {
			@WithName("url")
			String url();

			@WithName("serialId")
			String serialId();

			@WithName("secret")
			Optional<String> secret();
		}
	}

}
