package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

import java.io.File;
import java.util.Optional;

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
