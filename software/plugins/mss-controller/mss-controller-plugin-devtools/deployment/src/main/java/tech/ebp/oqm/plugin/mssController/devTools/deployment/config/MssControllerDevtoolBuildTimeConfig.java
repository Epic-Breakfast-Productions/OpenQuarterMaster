package tech.ebp.oqm.plugin.mssController.devTools.deployment.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.Constants;

@ConfigMapping(prefix = "quarkus." + Constants.CONFIG_ROOT_NAME)
@ConfigRoot(phase= ConfigPhase.BUILD_TIME)
public interface MssControllerDevtoolBuildTimeConfig {
	
	/**
	 * Config to manage the devservice stood up.
	 */
	MssControllerDevserviceConfig devServices();
}
