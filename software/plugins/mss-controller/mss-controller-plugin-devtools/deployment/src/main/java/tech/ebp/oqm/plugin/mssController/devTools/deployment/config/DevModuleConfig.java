package tech.ebp.oqm.plugin.mssController.devTools.deployment.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

import java.util.Optional;

@ConfigGroup
public interface DevModuleConfig {

	/**
	 * Info for the module
	 * @return
	 */
	MouduleInfo info();

	/**
	 * The interface type of this mss module
	 * @return
	 */
	@WithDefault("NETWORK")
	TestModuleType type();

	interface MouduleInfo{

		/**
		 * The spec version this module uses. Defaults to newest if not present
		 */
		Optional<String> specVersion();

		/**
		 * The serial id for this module. Auto generated if not specified.
		 * @return
		 */
		Optional<String> serialId();

		/**
		 * The manufacture date for this module. Auto generated if not specified.
		 * @return
		 */
		Optional<String> manufactureDate();

		/**
		 * The number of blocks supplied by the module.
		 * @return
		 */
		@WithDefault("64")
		Integer numBlocks();
	}

	public enum TestModuleType{
		SERIAL, NETWORK
	}
}
