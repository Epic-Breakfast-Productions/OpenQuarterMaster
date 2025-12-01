package tech.ebp.oqm.lib.core.api.quarkus.runtime;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Service to cache oqm db entries.
 *
 * Maybe move to core api extension?
 */
@Named("OqmDatabaseService")
@Slf4j
@ApplicationScoped
public class OqmDatabaseService {

	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;

	@Inject
	KcClientAuthService serviceAccountService;

	private final ReentrantLock mutex = new ReentrantLock();
	private ArrayNode dbs = null;
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
	@Scheduled(every = "{" + Constants.CONFIG_ROOT_NAME + ".caching.oqmDatabase.refreshFrequencyEvery}")
	public void refreshCache(){
		if(!enabled){
			return;
		}
		log.info("Refreshing cache of OQM databases.");
		ArrayNode newCacheData = this.oqmCoreApiClientService.manageDbList(this.serviceAccountService.getAuthString()).await().indefinitely();
		log.debug("Got new cache of databases: {}", newCacheData);
		try {
			this.mutex.lock();
			this.dbs = newCacheData;
		} finally {
			this.mutex.unlock();
		}
	}

	public ArrayNode getDatabases() {
		log.info("Getting cached OQM databases.");
		try {
			this.mutex.lock();
			return this.dbs;
		} finally {
			this.mutex.unlock();
		}
	}
}
