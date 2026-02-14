package tech.ebp.oqm.lib.core.api.quarkus.deployment.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

/**
 * Configuration for managing build-time related items.
 *
 *
 */
@ConfigMapping(prefix = Constants.CONFIG_ROOT_NAME, namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface CoreApiLibBuildTimeConfig {
	
	/**
	 * Configuring health options
	 * @return The health options
	 */
	HealthConfig health();
	
	/**
	 * Whether metrics are published in case a metrics extension is present.
	 * @return The metrics options
	 */
	@WithDefault("true")
	MetricsConfig metrics();
	
	/**
	 * Config to manage the devservice stood up.
	 * @return The devservice options
	 */
	DevserviceConfig devservice();
	
	/**
	 * Health options
	 */
	interface HealthConfig {
		
		/**
		 * Whether a health check is published in case the smallrye-health extension is present.
		 * @return Whether a health check is published in case the smallrye-health extension is present.
		 */
		@WithDefault("true")
		boolean enabled();
	}
	
	/**
	 * Metrics options.
	 */
	interface MetricsConfig {
		
		/**
		 * Whether a health check is published in case the smallrye-health extension is present.
		 * @return Whether a health check is published in case the smallrye-health extension is present.
		 */
		@WithDefault("true")
		boolean enabled();
	}
	
	/**
	 * Configuration for Core API devservices
	 */
	interface DevserviceConfig {
		
		/**
		 * Enable devservices
		 * @return if the devservices are enabled or not.
		 */
		@WithDefault("true")
		boolean enable();
		
		/**
		 * The version/ tag of the core api container image
		 * @return The version/ tag of the core api container image
		 */
		@WithDefault("4.4.0-SNAPSHOT")
		String coreApiVersion();
		
		/**
		 * Configuration for connecting to keycloak devservice setup by consuming service
		 * @return Configuration for connecting to keycloak devservice setup by consuming service
		 */
		KeycloakConfig keycloak();
		
		/**
		 * Configuration for connecting the devservice to an existing keycloak instance
		 */
		interface KeycloakConfig{
			
			/**
			 * The port of keycloak to look for
			 * @return The port of keycloak to look for
			 */
			@WithDefault("${quarkus.keycloak.devservices.port:9328}")
			Integer port();
			
			/**
			 * Realm we are using in keycloak
			 * @return Realm we are using in keycloak
			 */
			@WithDefault("${quarkus.keycloak.devservices.realm-name:oqm}")
			String realm();
		}
	}
}
