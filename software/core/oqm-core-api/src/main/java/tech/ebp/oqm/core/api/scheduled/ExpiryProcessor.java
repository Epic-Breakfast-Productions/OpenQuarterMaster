package tech.ebp.oqm.core.api.scheduled;

import com.mongodb.client.FindIterable;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Filters.size;

@Slf4j
@ApplicationScoped
public class ExpiryProcessor {
	
	@Inject
	HistoryEventNotificationService eventNotificationService;
	
	@Inject
	CoreApiInteractingEntity coreApiInteractingEntity;
	
	@Inject
	InventoryItemService inventoryItemService;

	@Inject
	OqmDatabaseService oqmDatabaseService;

	@Inject
	StoredService storedService;
	
	@WithSpan
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
			long result = this.storedService.scanForExpired(curEntry.getDbName());
			log.info("Processed {} expiry or warning events for {}", result, curEntry.getDbName());
		}
		log.info("Finished processing all held items for newly expired stored.");
	}
}
