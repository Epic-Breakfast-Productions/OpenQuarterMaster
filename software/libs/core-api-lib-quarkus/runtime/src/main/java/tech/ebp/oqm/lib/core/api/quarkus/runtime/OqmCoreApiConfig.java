package tech.ebp.oqm.lib.core.api.quarkus.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.WithDefault;

/**
 * https://github.com/quarkusio/quarkus/blob/main/extensions/mongodb-client/runtime/src/main/java/io/quarkus/mongodb/runtime/MongodbConfig.java
 */
@ConfigRoot(name = Constants.CONFIG_ROOT_NAME, phase = ConfigPhase.RUN_TIME)
public class OqmCoreApiConfig {
	
	/**
	 * The base uri for the OQM core API service. example: `<pre>http://host:port/</pre>`
	 */
	@ConfigItem(name = ConfigItem.ELEMENT_NAME)
	public String coreApiBaseUri;

	/**
	 * The frequency of which to refresh the cache of oqm databases.
	 */
	@ConfigItem(name = ConfigItem.ELEMENT_NAME, defaultValue = "30s")
	public String refreshDbCacheFrequency;


}
