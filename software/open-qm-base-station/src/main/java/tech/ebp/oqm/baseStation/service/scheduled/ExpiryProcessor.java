package tech.ebp.oqm.baseStation.service.scheduled;

import com.mongodb.client.FindIterable;
import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.notification.item.ItemEventNotificationDispatchService;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.ListAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.TrackedItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.StoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.ListAmountStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.trackedStored.TrackedMapStoredWrapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	
	private Optional<ItemExpiredEvent.Builder<?, ?>> processExpiryForStored(Stored stored) {
		if (!stored.getNotificationStatus().isExpired() && stored.getExpires() != null && LocalDate.now().isAfter(stored.getExpires())) {
			stored.getNotificationStatus().setExpired(true);
			return Optional.of(
				ItemExpiredEvent.builder()
			);
		}
		return Optional.empty();
	}
	
	private Optional<ItemExpiryWarningEvent.Builder<?, ?>> processExpiryWarningForStored(InventoryItem item, Stored stored) {
		if (
			!stored.getNotificationStatus().isExpiredWarning() &&
			!stored.getNotificationStatus().isExpired() &&
			stored.getExpires() != null &&
			!Duration.ZERO.equals(item.getExpiryWarningThreshold()) &&
			LocalDate.now().isAfter(stored.getExpires().minus(item.getExpiryWarningThreshold()))
		) {
			stored.getNotificationStatus().setExpiredWarning(true);
			return Optional.of(
				ItemExpiryWarningEvent.builder()
			);
		}
		return Optional.empty();
	}
	
	private List<? extends ItemExpiryEvent.Builder<?, ?>> processExpiryEvents(
		InventoryItem item,
		ObjectId storageBlockId,
		Stored stored
	) {
		List<ItemExpiryEvent.Builder<?, ?>> output = new ArrayList<>();
		
		{
			Optional<ItemExpiredEvent.Builder<?, ?>> expiredEvent = this.processExpiryForStored(stored);
			if (expiredEvent.isPresent()) {
				expiredEvent.get().storageBlockId(storageBlockId);
				output.add(expiredEvent.get());
			}
		}
		
		{
			Optional<ItemExpiryWarningEvent.Builder<?, ?>> expiryWarningEvent = this.processExpiryWarningForStored(item, stored);
			if (expiryWarningEvent.isPresent()) {
				expiryWarningEvent.get().storageBlockId(storageBlockId);
				output.add(expiryWarningEvent.get());
			}
		}
		
		return output;
	}
	
	private List<ItemExpiryEvent> processSimpleAmountItem(SimpleAmountItem item) {
		List<ItemExpiryEvent> events = new ArrayList<>();
		
		for (Map.Entry<ObjectId, SingleAmountStoredWrapper> curStored : item.getStorageMap().entrySet()) {
			List<? extends ItemExpiryEvent.Builder<?, ?>> results = processExpiryEvents(
				item,
				curStored.getKey(),
				curStored.getValue().getStored()
			);
			
			for (ItemExpiryEvent.Builder<?, ?> curEvent : results){
				events.add(curEvent.build());
			}
		}
		return events;
	}
	
	private List<ItemExpiryEvent> processListAmountItem(ListAmountItem item) {
		List<ItemExpiryEvent> events = new ArrayList<>();
		
		for (Map.Entry<ObjectId, ListAmountStoredWrapper> curStored : item.getStorageMap().entrySet()) {
			ListAmountStoredWrapper storedList = curStored.getValue();
			for (int i = 0; i < storedList.size(); i++) {
				List<? extends ItemExpiryEvent.Builder<?, ?>> results = processExpiryEvents(
					item,
					curStored.getKey(),
					storedList.get(i)
				);
				
				for (ItemExpiryEvent.Builder<?, ?> curEvent : results){
					curEvent.index(i);
					events.add(curEvent.build());
				}
			}
		}
		return events;
	}
	
	private List<ItemExpiryEvent> processTrackedItem(TrackedItem item) {
		List<ItemExpiryEvent> events = new ArrayList<>();
		
		for (Map.Entry<ObjectId, TrackedMapStoredWrapper> curStoredMap : item.getStorageMap().entrySet()) {
			for (Map.Entry<String, TrackedStored> curStored : curStoredMap.getValue().entrySet()) {
				List<? extends ItemExpiryEvent.Builder<?, ?>> results = processExpiryEvents(
					item,
					curStoredMap.getKey(),
					curStored.getValue()
				);
				
				for (ItemExpiryEvent.Builder<?, ?> curEvent : results){
					curEvent.identifier(curStored.getKey());
					events.add(curEvent.build());
				}
			}
		}
		return events;
	}
	
	public <T extends Stored, C, W extends StoredWrapper<C, T>> List<ItemExpiryEvent> processForExpired(InventoryItem<T, C, W> item) {
		List<ItemExpiryEvent> events;
		
		switch (item.getStorageType()) {
			case AMOUNT_SIMPLE:
				events = processSimpleAmountItem((SimpleAmountItem) item);
				break;
			case AMOUNT_LIST:
				events = processListAmountItem((ListAmountItem) item);
				break;
			case TRACKED:
				events = processTrackedItem((TrackedItem) item);
				break;
			default:
				throw new IllegalArgumentException("Should not have been able to get unsupported storage type: " + item.getStorageType());
		}
		return events;
	}
	
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
			List<ItemExpiryEvent> expiryEvents = this.processForExpired(cur);
			
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
