package tech.ebp.oqm.lib.core.api.quarkus.runtime;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Service to cache oqm db entries.
 */
@Named("OqmUnitService")
@Slf4j
@ApplicationScoped
public class OqmUnitService {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;
	
	@Inject
	KcClientAuthService serviceAccountService;
	
	/**
	 * The mutex to protect held cache
	 */
	private final ReentrantLock mutex = new ReentrantLock();
	
	/**
	 * The set of all units that are supported
	 */
	private ObjectNode allUnits = null;
	
	/**
	 * The unit compatibility map to show which units are compatible with which other units
	 */
	private ObjectNode unitCompatibilityMap = null;
	
	/**
	 * If this service is enabled or not.
	 */
	private boolean enabled = false;
	
	@PostConstruct
	public void setup() {
		if (
			ConfigProvider.getConfig().getOptionalValue("quarkus.oidc.client-id", String.class).isEmpty() ||
			ConfigProvider.getConfig().getOptionalValue("quarkus.oidc.credentials.secret", String.class).isEmpty()
		) {
			log.warn("No OIDC creds. Caching units disabled.");
			return;
		}
		this.enabled = true;
		
		this.refreshCache();
	}
	
	/**
	 * Scheduled method to refresh the cache.
	 * <p>
	 * TODO:: instead of this, watch for message? Both?
	 */
	@Scheduled(every = "{" + Constants.CONFIG_ROOT_NAME + ".caching.unit.refreshFrequencyEvery}")
	public void refreshCache() {
		if (!enabled) {
			return;
		}
		log.info("Refreshing cache of OQM units.");
		
		ObjectNode allUnits = this.oqmCoreApiClientService.unitGetAll(this.serviceAccountService.getAuthString()).await().indefinitely();
		ObjectNode unitCompatibilityMap = this.oqmCoreApiClientService.unitGetCompatibleMap(this.serviceAccountService.getAuthString()).await().indefinitely();
		
		log.debug("Got new cache of units: {}", allUnits);
		try {
			this.mutex.lock();
			this.allUnits = allUnits;
			this.unitCompatibilityMap = unitCompatibilityMap;
		} finally {
			this.mutex.unlock();
		}
	}
	
	/**
	 * Gets all the cached OQM units.
	 * @return The set of all OQM units we have in cache.
	 */
	public ObjectNode getAllUnits() {
		log.info("Getting cached OQM units.");
		try {
			this.mutex.lock();
			return this.allUnits;
		} finally {
			this.mutex.unlock();
		}
	}
	
	/**
	 * Gets all the cached OQM unit compatibility map.
	 * @return The unit compatibility map to show which units are compatible with which other units
	 */
	public ObjectNode getCompatibleUnitMap() {
		log.info("Getting cached OQM unit compatibility map.");
		try {
			this.mutex.lock();
			return this.unitCompatibilityMap;
		} finally {
			this.mutex.unlock();
		}
	}
	
	/**
	 * Gets the units compatible with the given unit
	 * @param unit The unit to get the map entry for
	 * @return The list of units that are compatible with the given one.
	 */
	public ArrayNode getUnitCompatibleWith(String unit) {
		return (ArrayNode) this.getCompatibleUnitMap().get(unit);
	}
}
