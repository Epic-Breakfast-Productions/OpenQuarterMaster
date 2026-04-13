package tech.ebp.oqm.core.api.scheduled;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.service.ItemStatsService;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

/**
 * Scheduled processor for handling item expiry events.
 * <p>
 * Runs periodically to scan all databases for items that have expired or are approaching
 * their expiry dates. For each database, it triggers expiry/warning event processing
 * via {@link ItemStatsService#scanForExpired(String)}.
 * </p>
 * <p>
 * Configuration: The execution schedule is controlled by the {@code service.item.expiryCheck.cron}
 * property. Concurrent executions are skipped if one is already running.
 * </p>
 */
@Slf4j
@ApplicationScoped
public class ExpiryProcessor {

	@Inject
	OqmDatabaseService oqmDatabaseService;

	
	@Inject
	ItemStatsService itemStatsService;
	
	/**
	 * Main scheduled task that processes expiry events for all databases.
	 * <p>
	 * Iterates through all registered databases and calls the item stats service
	 * to scan for expired items and generate appropriate events.
	 * </p>
	 */
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
