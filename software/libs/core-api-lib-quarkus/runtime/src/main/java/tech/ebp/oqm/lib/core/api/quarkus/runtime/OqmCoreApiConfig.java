package tech.ebp.oqm.lib.core.api.quarkus.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.mutiny.Uni;

/**
 * https://github.com/quarkusio/quarkus/blob/main/extensions/mongodb-client/runtime/src/main/java/io/quarkus/mongodb/runtime/MongodbConfig.java
 */
@ConfigMapping(prefix = Constants.CONFIG_ROOT_NAME, namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface OqmCoreApiConfig {
	
	/**
	 * The base uri for the OQM core API service. example: `<pre>http://host:port/</pre>`
	 */
	String baseUri();
	
	/**
	 * The frequency of which to refresh the cache of oqm databases.
	 */
	CachingConfig caching();
	
	interface CachingConfig {
		
		/**
		 * Cache config for the list of OQM Databases.
		 */
		OqmDatabaseConfig oqmDatabase();
		
		/**
		 * Cache config for the list of OQM Units.
		 * @return
		 */
		UnitConfig unit();
		
		/**
		 * Cache config for the list of OQM Units.
		 * @return
		 */
		InfoConfig info();
		
		interface OqmDatabaseConfig{
			/**
			 * The frequency of which to refresh the cache of oqm databases.
			 */
			@WithDefault("30s")
			String refreshFrequencyEvery();
		}
		interface UnitConfig{
			/**
			 * The frequency of which to refresh the cache of oqm databases.
			 */
			@WithDefault("1m")
			String refreshFrequencyEvery();
		}
		interface InfoConfig{
			/**
			 * The frequency of which to refresh the cache of oqm databases.
			 */
			@WithDefault("10m")
			String refreshFrequencyEvery();
		}
	}
}
