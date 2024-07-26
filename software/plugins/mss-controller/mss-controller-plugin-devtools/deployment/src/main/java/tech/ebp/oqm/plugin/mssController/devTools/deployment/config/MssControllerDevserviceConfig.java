package tech.ebp.oqm.plugin.mssController.devTools.deployment.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithDefaults;

import java.util.List;
import java.util.Optional;

@ConfigGroup
public class MssControllerDevserviceConfig {
	/**
	 * Enable devservices
	 */
	@WithDefault("true")
	public boolean enable;

	/**
	 * The modules to set up for dev tooling
	 */
	public Optional<DevModuleConfig> modules;
}
