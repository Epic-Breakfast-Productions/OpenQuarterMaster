package tech.ebp.oqm.lib.core.api.quarkus.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * https://github.com/quarkusio/quarkus/blob/main/extensions/mongodb-client/runtime/src/main/java/io/quarkus/mongodb/runtime/MongodbConfig.java
 */
@ConfigRoot(name = "oqmCoreApiLib", phase = ConfigPhase.RUN_TIME)
public class OqmCoreApiConfig {
	
	/**
	 * The base uri for the OQM core API service. example: `<pre>http://host:port/</pre>`
	 */
	@ConfigItem(name = ConfigItem.ELEMENT_NAME)
	public String coreApiBaseUri;
}
