package tech.ebp.oqm.plugin.mssController.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
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

	@WithName("management")
	ManagementConfig management();

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

			@WithDefault("5s")
			Duration commandResponseTimeout();
		}

		interface SerialModuleConfig {
			@WithName("portPath")
			Path portPath();

			@WithName("baudRate")
			Optional<Integer> baudRate();
		}

		interface ScanConfig {
			@WithDefault("false")
			boolean enabled();

			@WithName("scanDirs")
			@WithDefault("/dev/")//TODO:: doublecheck
			List<String> scanDirs();
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

	interface ManagementConfig {
		@WithName("scheduledEvery")
		@WithDefault("1s")
		String every();

		@WithName("failTimeout")
		@WithDefault("10s")
		Duration failTimeout();
	}

}
