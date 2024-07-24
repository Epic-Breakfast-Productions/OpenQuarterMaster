package tech.ebp.oqm.plugin.mssController.devTools.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.Constants;

@ConfigRoot(name= Constants.CONFIG_ROOT_NAME, phase= ConfigPhase.BUILD_TIME)
public class CoreApiLibBuildTimeConfig {
	/**
	 * Whether a health check is published in case the smallrye-health extension is present.
	 */
	@ConfigItem(name = "health.enabled", defaultValue = "true")
	public boolean healthEnabled;
	
	/**
	 * Whether metrics are published in case a metrics extension is present.
	 */
	@ConfigItem(name = "metrics.enabled")
	public boolean metricsEnabled;
	
	/**
	 * Config to manage the devservice stood up.
	 * @return
	 */
	@ConfigItem(name="devservice")
	public CoreApiLibDevserviceConfig devservice;
}
