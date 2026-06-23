package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.Constants;

/**
 * https://github.com/quarkusio/quarkus/blob/main/extensions/mongodb-client/runtime/src/main/java/io/quarkus/mongodb/runtime/MongodbConfig.java
 */
@ConfigMapping(prefix = Constants.CONFIG_ROOT_NAME, namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface OqmCoreCharacteristicsConfig {
	/**
	 * The base uri for the OQM core API service. example: `<pre>http://host:port/</pre>`
	 */
	@WithDefault(" ")
	String baseUri();

	/**
	 * The category of the consuming service this is. Used for UI display.
	 */
	String serviceCategory();

	/** The id of the consuming service. Used for UI display. */
	String serviceId();
}
