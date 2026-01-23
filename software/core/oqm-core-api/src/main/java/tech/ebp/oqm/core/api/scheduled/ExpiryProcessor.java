package tech.ebp.oqm.core.api.scheduled;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.service.ItemStatsService;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

@Slf4j
@ApplicationScoped
public class ExpiryProcessor {

	@Inject
	OqmDatabaseService oqmDatabaseService;

	
	@Inject
	ItemStatsService itemStatsService;
	
	@Scheduled(
		identity = "searchAndProcessExpiredItems",
		cron = "{service.item.expiryCheck.cron}",
		concurrentExecution = Scheduled.ConcurrentExecution.SKIP
	)
	public void searchAndProcessExpiring() {
		//TODO:: mutex lock
		//TODO:: multithread
		log.info("Start processing all held items for newly expired stored.");
		for(DbCacheEntry curEntry : this.oqmDatabaseService.getDatabases()){
			log.info("Processing expiry or warning events for {}", curEntry.getDbName());
			long result = this.itemStatsService.scanForExpired(curEntry.getDbName());
			log.info("Processed {} expiry or warning events for {}", result, curEntry.getDbName());
		}
		log.info("Finished processing all held items for newly expired stored.");
	}
}
