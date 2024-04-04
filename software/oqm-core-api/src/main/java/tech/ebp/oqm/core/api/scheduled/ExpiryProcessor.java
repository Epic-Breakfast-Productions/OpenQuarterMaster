package tech.ebp.oqm.core.api.scheduled;

import com.mongodb.client.FindIterable;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.config.BaseStationInteractingEntity;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

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
	BaseStationInteractingEntity baseStationInteractingEntity;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@WithSpan
	@Scheduled(
		identity = "searchAndProcessExpiredItems",
		cron = "{service.item.expiryCheck.cron}",
		concurrentExecution = Scheduled.ConcurrentExecution.SKIP
	)
	public void searchAndProcessExpiring() {
		log.info("Start processing all held items for newly expired stored.");
		
		FindIterable<InventoryItem> it = this.inventoryItemService.listIterator(
			and(
				not(size("storageMap", 0))
				//TODO:: figure out better filter
				//  - https://stackoverflow.com/a/26967000/3015723
				//  - https://stackoverflow.com/a/71999502/3015723
			),
			null,
			null
		);
		
		it.forEach((InventoryItem cur)->{
			List<ItemExpiryEvent> expiryEvents = cur.updateExpiredStates();
			
			if (!expiryEvents.isEmpty()) {
				this.inventoryItemService.update(cur);
				for (ItemExpiryEvent curEvent : expiryEvents) {
					curEvent.setEntity(this.baseStationInteractingEntity.getId());
					this.inventoryItemService.addHistoryFor(cur, null, curEvent);//TODO:: pass BS entity?
					this.eventNotificationService.sendEvent(this.inventoryItemService.getClazz(), curEvent);//TODO:: handle potential threadedness?
				}
			}
		});
		log.info("Finished processing all held items for newly expired stored.");
	}
}
