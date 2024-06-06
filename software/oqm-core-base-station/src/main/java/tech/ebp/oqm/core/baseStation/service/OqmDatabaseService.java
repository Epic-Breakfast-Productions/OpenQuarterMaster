package tech.ebp.oqm.core.baseStation.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.service.sso.KcClientAuthService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Service to cache oqm db entries.
 *
 * Maybe move to core api extension?
 */
@Slf4j
@ApplicationScoped
public class OqmDatabaseService {

	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;

	@Inject
	KcClientAuthService serviceAccountService;

	private final ReentrantLock mutex = new ReentrantLock();
	private ArrayNode dbs = null;

	@PostConstruct
	public void setup(){
		this.refreshCache();
	}

	//TODO:: instead of this, watch for message? Both?
	@Scheduled(every = "{service.refreshDbCacheFrequency}")
	public void refreshCache(){
		log.info("Refreshing cache of OQM databases.");
		ArrayNode newCacheData = this.oqmCoreApiClientService.manageDbList(this.serviceAccountService.getAuthString()).await().indefinitely();
		log.info("Got new cache of databases: {}", newCacheData);
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