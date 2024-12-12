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
 *
 * Maybe move to core api extension?
 */
@Named("OqmUnitService")
@Slf4j
@ApplicationScoped
public class OqmUnitService {

	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;

	@Inject
	KcClientAuthService serviceAccountService;

	private final ReentrantLock mutex = new ReentrantLock();
	private ObjectNode allUnits = null;
	private ObjectNode unitCompatibilityMap = null;
	private boolean enabled = false;

	@PostConstruct
	public void setup(){
		if(
			ConfigProvider.getConfig().getOptionalValue("quarkus.oidc.client-id", String.class).isEmpty() ||
			ConfigProvider.getConfig().getOptionalValue("quarkus.oidc.credentials.secret", String.class).isEmpty()
		){
			log.info("No OIDC creds. Disabled.");
			return;
		}
		this.enabled = true;

		this.refreshCache();
	}

	//TODO:: instead of this, watch for message? Both?
	@Scheduled(every = "{quarkus." + Constants.CONFIG_ROOT_NAME + ".refreshDbCacheFrequency}")
	public void refreshCache(){
		if(!enabled){
			return;
		}
		log.info("Refreshing cache of OQM units.");

		ObjectNode allUnits = this.oqmCoreApiClientService.unitGetAll(this.serviceAccountService.getAuthString()).await().indefinitely();
		ObjectNode unitCompatibilityMap = this.oqmCoreApiClientService.unitGetCompatibleMap(this.serviceAccountService.getAuthString()).await().indefinitely();

		ArrayNode newCacheData = this.oqmCoreApiClientService.manageDbList(this.serviceAccountService.getAuthString()).await().indefinitely();
		log.info("Got new cache of units: {}", newCacheData);
		try {
			this.mutex.lock();
			this.allUnits = allUnits;
			this.unitCompatibilityMap = unitCompatibilityMap;
		} finally {
			this.mutex.unlock();
		}
	}

	public ObjectNode getAllUnits() {
		log.info("Getting cached OQM units.");
		try {
			this.mutex.lock();
			return this.allUnits;
		} finally {
			this.mutex.unlock();
		}
	}
	public ObjectNode getCompatibleUnitMap() {
		log.info("Getting cached OQM unit compatibility map.");
		try {
			this.mutex.lock();
			return this.unitCompatibilityMap;
		} finally {
			this.mutex.unlock();
		}
	}

	public ArrayNode getUnitCompatibleWith(String unit) {
		return (ArrayNode) this.getCompatibleUnitMap().get(unit);
	}
}
