package tech.ebp.oqm.plugin.mssController.devTools.deployment.config;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.Constants;

@ConfigRoot(name= Constants.CONFIG_ROOT_NAME, phase= ConfigPhase.BUILD_TIME)
public class MssControllerDevtoolBuildTimeConfig {
	
	/**
	 * Config to manage the devservice stood up.
	 */
	@ConfigItem(name="devservice")
	public MssControllerDevserviceConfig devservice;
}
