package tech.ebp.oqm.lib.core.characteristics.quarkus.deployment.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import org.testcontainers.utility.DockerImageName;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.Constants;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Configuration for managing build-time related items.
 *
 *
 */
@ConfigMapping(prefix = Constants.CONFIG_ROOT_NAME, namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface CoreCharacteristicsLibBuildTimeConfig {
	
	
	/**
	 * The category of the consuming service this is. Used for UI display.
	 */
	String serviceCategory();
	
	/** The id of the consuming service. Used for UI display. */
	String serviceId();
	
	/**
	 * Configuring health options
	 * @return The health options
	 */
	HealthConfig health();
	
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
	 * Configuration for Core API devservices
	 */
	interface DevserviceConfig {
		
		/**
		 * Enable devservices
		 * @return if the devservices are enabled or not.
		 */
		@WithDefault("true")
		boolean enable();
		
		// TODO:: configure RunBy, UIs, images
		
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
			@WithDefault("1.0.0")
			String version();
			
			/**
			 * The name of the core api container image to use.
			 *
			 * @return The name of the core api container image to use.
			 */
			@WithDefault("docker.io/ebprod/oqm-core-characteristics")
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
		
		/** Set characteristic and other data for the system */
		DevData devData();
		
		interface DevData {
			
			/**
			 * Configuration for the characteristics service
			 * @return
			 */
			CharacteristicsConfig characteristics();
			
			interface CharacteristicsConfig {
				/** The name of the system */
				Optional<String> title();
				/** The message of the day for the system */
				Optional<String> motd();
				
				/** Details describing who is running the system */
				RunByConfig runBy();
				
				interface RunByConfig {
					/** The name of who runs the system */
					Optional<String> name();
					/** The email of who runs the system */
					Optional<String> email();
					/** The phone of who runs the system */
					Optional<String> phone();
					/** The website of who runs the system */
					Optional<String> website();
					
					/** The logo of who runs the system */
					@WithDefault("false")
					boolean haveLogoImg();
					/** The banner image of who runs the system */
					@WithDefault("false")
					boolean haveBannerImg();
				}
				
				/** Banner information */
				BannerConfig banner();
				
				interface BannerConfig {
					/** The text to be displayed in the banner */
					Optional<String> text();
					/** The color of the text to be displayed in the banner */
					Optional<String> textColor();
					/** The background color of the banner */
					Optional<String> backgroundColor();
				}
			}
			
			/** Other UIs in the system */
			UisConfig uis();
			
			interface UisConfig {
				/** The homepage of the system */
				Optional<String> home();
				
				//TODO:: uis
			}
		}
		
	}
}
