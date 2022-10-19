package tech.ebp.oqm.baseStation.scheduled;

import com.mongodb.client.FindIterable;
import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.notification.item.ItemEventNotificationDispatchService;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Filters.size;

@Traced
@Slf4j
@ApplicationScoped
public class ExpiryProcessor {
	
	@Inject
	ItemEventNotificationDispatchService iends;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@Traced
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
					inventoryItemService.addHistoryFor(cur, curEvent);
					iends.sendEvent(cur, curEvent);//TODO:: handle potential threadedness?
				}
			}
		});
		log.info("Finished processing all held items for newly expired stored.");
	}
}
