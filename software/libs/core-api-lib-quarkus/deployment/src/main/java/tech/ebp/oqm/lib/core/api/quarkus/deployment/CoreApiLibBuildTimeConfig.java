package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

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
	 * Configuration for DevServices. DevServices allows Quarkus to automatically start MongoDB in dev and test mode.
	 */
//	@ConfigItem
//	public DevServicesBuildTimeConfig devservices; TODO https://github.com/quarkusio/quarkus/blob/main/extensions/mongodb-client/deployment/src/main/java/io/quarkus/mongodb/deployment/DevServicesBuildTimeConfig.java
}
