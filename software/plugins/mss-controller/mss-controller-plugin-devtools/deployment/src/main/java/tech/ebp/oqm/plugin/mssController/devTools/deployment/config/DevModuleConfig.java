package tech.ebp.oqm.plugin.mssController.devTools.deployment.config;

import io.quarkus.runtime.annotations.ConfigGroup;

import java.util.Optional;

@ConfigGroup
public class DevModuleConfig {

	/**
	 * The spec version this module uses.
	 */
	public Optional<String> specVersion;
}
