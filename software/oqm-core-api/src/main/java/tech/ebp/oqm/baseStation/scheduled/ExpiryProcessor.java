package tech.ebp.oqm.baseStation.scheduled;

import com.mongodb.client.FindIterable;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.baseStation.config.BaseStationInteractingEntity;
import tech.ebp.oqm.baseStation.model.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.notification.item.ItemEventNotificationDispatchService;

import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Filters.size;

@Slf4j
@ApplicationScoped
public class ExpiryProcessor {
	
	@Inject
	ItemEventNotificationDispatchService iends;
	
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
				inventoryItemService.update(cur);
				for (ItemExpiryEvent curEvent : expiryEvents) {
					curEvent.setEntity(this.baseStationInteractingEntity.getId());
					inventoryItemService.addHistoryFor(cur, null, curEvent);
					iends.sendEvent(cur, curEvent);//TODO:: handle potential threadedness?
				}
			}
		});
		log.info("Finished processing all held items for newly expired stored.");
	}
}
