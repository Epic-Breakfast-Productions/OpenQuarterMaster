package tech.ebp.oqm.baseStation.service.scheduled;

import com.mongodb.client.FindIterable;
import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemExpiredEvent;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.ListAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.TrackedItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Filters.size;

@Slf4j
@ApplicationScoped
public class ExpiryProcessor {
	
	@Inject
	InventoryItemService inventoryItemService;
	
	private Optional<ItemExpiredEvent> processExpiryForStored(ObjectId storageBlockId, Stored stored) {
		if (!stored.isExpired() && stored.getExpires() != null && LocalDate.now().isAfter(stored.getExpires())) {
			return Optional.of(
				ItemExpiredEvent.builder()
								.storageBlockId(storageBlockId)
								.build()
			);
		}
		return Optional.empty();
	}
	
	private List<ItemExpiredEvent> processSimpleAmountStored(Map<ObjectId, AmountStored> storageMap) {
		List<ItemExpiredEvent> events = new ArrayList<>();
		
		for (Map.Entry<ObjectId, AmountStored> curStored : storageMap.entrySet()) {
			Optional<ItemExpiredEvent> event = this.processExpiryForStored(
				curStored.getKey(),
				curStored.getValue()
			);
			event.ifPresent(events::add);
		}
		return events;
	}
	
	private List<ItemExpiredEvent> processListAmountStored(Map<ObjectId, List<AmountStored>> storageMap) {
		List<ItemExpiredEvent> events = new ArrayList<>();
		
		for (Map.Entry<ObjectId, List<AmountStored>> curStored : storageMap.entrySet()) {
			List<AmountStored> storedList = curStored.getValue();
			for (int i = 0; i < storedList.size(); i++) {
				Optional<ItemExpiredEvent> eventOp = this.processExpiryForStored(
					curStored.getKey(),
					storedList.get(i)
				);
				if (eventOp.isPresent()) {
					ItemExpiredEvent event = eventOp.get();
					event.setIndex(i);
					events.add(event);
				}
			}
		}
		return events;
	}
	
	private List<ItemExpiredEvent> processTrackedAmountStored(Map<ObjectId, Map<String, TrackedStored>> storageMap) {
		List<ItemExpiredEvent> events = new ArrayList<>();
		
		for (Map.Entry<ObjectId, Map<String, TrackedStored>> curStoredMap : storageMap.entrySet()) {
			for (Map.Entry<String, TrackedStored> curStored : curStoredMap.getValue().entrySet()) {
				Optional<ItemExpiredEvent> eventOp = this.processExpiryForStored(curStoredMap.getKey(), curStored.getValue());
				
				if (eventOp.isPresent()) {
					ItemExpiredEvent event = eventOp.get();
					event.setIdentifier(curStored.getKey());
					events.add(event);
				}
			}
		}
		return events;
	}
	
	public <T> List<ItemExpiredEvent> processForExpired(InventoryItem<T> item) {
		List<ItemExpiredEvent> events;
		
		switch (item.getStorageType()) {
			case AMOUNT_SIMPLE:
				events = processSimpleAmountStored(((SimpleAmountItem) item).getStorageMap());
				break;
			case AMOUNT_LIST:
				events = processListAmountStored(((ListAmountItem) item).getStorageMap());
				break;
			case TRACKED:
				events = processTrackedAmountStored(((TrackedItem) item).getStorageMap());
				break;
			default:
				throw new IllegalArgumentException("Should not have been able to get unsupported storage type: " + item.getStorageType());
		}
		return events;
	}
	
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
			),
			null,
			null
		);
		
		it.forEach((InventoryItem cur)->{
			List<ItemExpiredEvent> expiryEvents = this.processForExpired(cur);
			
			if (!expiryEvents.isEmpty()) {
				inventoryItemService.update(cur);
				for (ItemExpiredEvent curEvent : expiryEvents) {
					inventoryItemService.addHistoryFor(cur, curEvent);
				}
			}
		});
	}
}
