package tech.ebp.oqm.core.baseStation.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@ApplicationScoped
public class OqmDatabaseService {

	@Inject
	OqmCoreApiClientService oqmCoreApiClientService;

	@Inject
	ServiceAccountService serviceAccountService;

	private final ReentrantLock mutex = new ReentrantLock();
	private ArrayNode dbs = null;

	@Scheduled(every = "1m")
	public void refreshCache(){
		log.info("Refreshing cache of OQM databases.");
		try {
			mutex.lock();
			this.dbs = this.oqmCoreApiClientService.manageDbList(this.serviceAccountService.getSAToken()).await().indefinitely();
		} finally {
			this.mutex.unlock();
		}
	}

	public ArrayNode getDatabases() {
		try {
			mutex.lock();
			return this.dbs;
		} finally {
			this.mutex.unlock();
		}
	}
}
