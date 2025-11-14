package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

@ConfigMapping(prefix = Constants.CONFIG_ROOT_NAME, namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface CoreApiLibBuildTimeConfig {
	
	/**
	 * Configuring health options
	 */
	HealthConfig health();
	
	/**
	 * Whether metrics are published in case a metrics extension is present.
	 */
	@WithDefault("true")
	MetricsConfig metrics();
	
	/**
	 * Config to manage the devservice stood up.
	 */
	DevserviceConfig devservice();
	
	interface HealthConfig {
		
		/**
		 * Whether a health check is published in case the smallrye-health extension is present.
		 */
		@WithDefault("true")
		boolean enabled();
	}
	
	interface MetricsConfig {
		
		/**
		 * Whether a health check is published in case the smallrye-health extension is present.
		 */
		@WithDefault("true")
		boolean enabled();
	}
	
	interface DevserviceConfig {
		
		/**
		 * Enable devservices
		 */
		@WithDefault("true")
		boolean enable();
		
		/**
		 * The version/ tag of the core api container image
		 */
		@WithDefault("4.0.1-DEV")
		String coreApiVersion();
		
		/**
		 * Configuration for connecting to keycloak devservice setup by consuming service
		 */
		KeycloakConfig keycloak();
		
		interface KeycloakConfig{
			
			/**
			 * The port of keycloak to look for
			 */
			@WithDefault("${quarkus.keycloak.devservices.port:9328}")
			Integer port();
			
			/**
			 * Realm we are using in keycloak
			 */
			@WithDefault("${quarkus.keycloak.devservices.realm-name:oqm}")
			String realm();
		}
	}
}
