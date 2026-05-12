package tech.ebp.oqm.lib.core.api.quarkus.deployment.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import org.testcontainers.utility.DockerImageName;
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
	DevserviceConfig devservices();
	
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
		 * The host port to assign to the core api devservice
		 * @return The host port assigned to the core api devservice
		 */
		@WithDefault("8123")
		Integer port();
		
		/**
		 * Configuration for the core api container image
		 * @return Configuration for the core api container image
		 */
		ImageConfig image();
		
		/**
		 * Configuration for the core api container image
		 */
		interface ImageConfig {
			
			/**
			 * The version/ tag of the core api container image
			 *
			 * @return The version/ tag of the core api container image
			 */
			@WithDefault("4.4.7")
			String version();
			
			/**
			 * The name of the core api container image to use.
			 *
			 * @return The name of the core api container image to use.
			 */
			@WithDefault("docker.io/ebprod/oqm-core-api")
			String name();
			
			/**
			 * Converts this config into the object that testcontainers expects.
			 *
			 * @return A configured DockerImageName object
			 */
			default DockerImageName toTestContainerImageName() {
				return DockerImageName.parse(this.name() + ":" + this.version());
			}
		}
		
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
		
		/**
		 * Configuration for connecting to keycloak devservice setup by consuming service
		 * @return Configuration for connecting to keycloak devservice setup by consuming service
		 */
		KafkaConfig kafka();
		
		/**
		 * Configuration for connecting the devservice to an existing keycloak instance
		 */
		interface KafkaConfig {
			
			/**
			 * Whether kafka devservices are enabled, and should connect the core api service to it
			 * @return If the core api should connect to the kafka devservice
			 */
			@WithDefault("${quarkus.kafka.devservices.enabled:false}")
			boolean enabled();
			
			/**
			 * The port of kafka to look for
			 * @return The port of kafka to look for
			 */
			@WithDefault("${quarkus.kafka.devservices.port:9192}")
			Integer port();
			
		}
	}
}
