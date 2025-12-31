package tech.ebp.oqm.core.api.service;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.processing.ItemExpiryLowStockItemProcessResults;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.processing.ItemPostTransactionProcessResults;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.processing.StoredExpiryLowStockProcessResult;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.CalculatedPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.Pricing;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.StoredPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.TotalPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.BasicStatsContaining;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.ItemStoredStats;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.StatsWithTotalContaining;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.StoredInBlockStats;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.StoredStats;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TODO:: update to include pricing #1006
 */
@Slf4j
@ApplicationScoped
public class ItemStatsService {
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	CoreApiInteractingEntity coreApiInteractingEntity;
	
	@Getter
	@Inject
	InventoryItemService inventoryItemService;
	
	@Getter
	@Inject
	StoredService storedService;
	
	
	private void addToStats(InventoryItem item, BasicStatsContaining statsToAddTo, Stored stored) {
		
		statsToAddTo.setNumStored(statsToAddTo.getNumStored() + 1L);
		
		if (stored.getType() == StoredType.AMOUNT) {
			AmountStored amtStored = (AmountStored) stored;
			if (amtStored.getLowStockThreshold() != null && stored.getNotificationStatus().isLowStock()) {
				statsToAddTo.setNumLowStock(statsToAddTo.getNumLowStock() + 1L);
			}
		}
		
		if (stored.getNotificationStatus().isExpiredWarning()) {
			statsToAddTo.setNumExpiryWarn(statsToAddTo.getNumExpiryWarn() + 1L);
		}
		
		if (stored.getNotificationStatus().isExpired()) {
			statsToAddTo.setNumExpired(statsToAddTo.getNumExpired() + 1L);
		}
		
		/*
		 * Prices
		 */
		LinkedHashSet<StoredPricing> storedPrices = new LinkedHashSet<>(stored.getPrices());
		//add prices not in stored's from item
		for(StoredPricing itemPrice : item.getDefaultPrices()){
			if(
				storedPrices.stream()
					.anyMatch((price)->{
						return price.getLabel().equals(itemPrice.getLabel());
					})
			){
				storedPrices.add(itemPrice);
			}
		}
		//add each to stats
		for(StoredPricing curPrice : storedPrices){
			CalculatedPricing calcedPricing = curPrice.calculatePrice(stored);
			
			Optional<TotalPricing> existingPricing = statsToAddTo.getPrices().stream()
				.filter((price)->{
					return price.getLabel().equals(curPrice.getLabel());
				})
				.findFirst();
			
			if(existingPricing.isEmpty()){
				statsToAddTo.getPrices().add(
					TotalPricing.builder()
						.label(curPrice.getLabel())
						.totalPrice(calcedPricing.getTotalPrice())
						.build()
				);
			} else {
				existingPricing.get().add(calcedPricing);
			}
		}
	}
	
	private void addToStats(InventoryItem item, StatsWithTotalContaining statsToAddTo, Stored stored) {
		this.addToStats(item, (BasicStatsContaining) statsToAddTo, stored);
		
		Quantity toAdd = switch (stored.getType()) {
			case AMOUNT -> {
				AmountStored amountStored = (AmountStored) stored;
				yield amountStored.getAmount();
			}
			case UNIQUE -> UnitUtils.Quantities.UNIT_ONE;
		};
		
		statsToAddTo.setTotal(
			statsToAddTo.getTotal().add(toAdd)
		);
	}
	
	private void addToStats(InventoryItem item, StoredInBlockStats storedInBlockStats, Stored stored) {
		storedInBlockStats.setHasStored(true);
		this.addToStats(item, (StatsWithTotalContaining) storedInBlockStats, stored);
	}
	
	private void addToStats(InventoryItem item, ItemStoredStats itemStoredStats, Stored stored) {
		StoredInBlockStats storedInBlockStats = itemStoredStats.getStorageBlockStats().get(stored.getStorageBlock());
		
		this.addToStats(item, storedInBlockStats, stored);
		this.addToStats(item, (StatsWithTotalContaining) itemStoredStats, stored);
	}
	
	//TODO:: wtf is this
//	private void addToStats(String oqmDbIdOrName, ClientSession cs, StoredStats storedStats, Stored stored) {
//
//		if (!storedStats.getItemStats().containsKey(stored.getId())) {
//			storedStats.getItemStats().put(
//				stored.getId(), new ItemStoredStats(
//					this.inventoryItemService.get(oqmDbIdOrName, cs, stored.getItem()).getUnit()
//				)
//			);
//		}
//		ItemStoredStats itemStoredStats = storedStats.getItemStats().get(stored.getItem());
//
//		this.addToStats((BasicStatsContaining) storedStats, stored);
//		this.addToStats(itemStoredStats, stored);
//	}
//	public StoredStats getStoredStats(String oqmDbIdOrName, ClientSession cs, StoredSearch search) {
//		FindIterable<Stored> storedInItem = this.getStoredService().listIterator(oqmDbIdOrName, cs, search);
//		StoredStats output = new StoredStats();
//
//		try (
//			MongoCursor<Stored> storedIterator = storedInItem.iterator()
//		) {
//			while (storedIterator.hasNext()) {
//				Stored curStored = storedIterator.next();
//
//				this.addToStats(
//					oqmDbIdOrName,
//					cs,
//					output,
//					curStored
//				);
//			}
//		}
//
//		return output;
//	}
	
	public ItemStoredStats getItemStats(String oqmDbIdOrName, ClientSession cs, InventoryItem item) {
		log.info("Getting stats for item: {}", item.getId());
		
		ItemStoredStats output = new ItemStoredStats(item.getUnit());
		
		for (ObjectId storageBlock : item.getStorageBlocks()) {
			output.getStorageBlockStats().put(storageBlock, new StoredInBlockStats(output.getTotal().getUnit()));
		}
		
		if(item.getId() != null) {
			FindIterable<Stored> storedInItem = this.getStoredService().listIterator(oqmDbIdOrName, cs, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
			try (
				MongoCursor<Stored> storedIterator = storedInItem.iterator()
			) {
				while (storedIterator.hasNext()) {
					Stored curStored = storedIterator.next();
					
					this.addToStats(
						item,
						output,
						curStored
					);
				}
			}
		}
		log.info("Finished getting stats for item: {}", item.getId());
		
		return output;
	}
	
	public ItemStoredStats getItemStats(String oqmDbIdOrName, ClientSession cs, ObjectId itemId) {
		InventoryItem item = this.inventoryItemService.get(oqmDbIdOrName, cs, itemId);
		return this.getItemStats(oqmDbIdOrName, cs, item);
	}
	
	/**
	 * Processes a stored item to determine if expired/expiring or low stock.
	 *
	 * If flags changed, updates stored object in database, with history details passed.
	 *
	 * @param oqmDbIdOrName
	 * @param cs
	 * @param stored
	 * @param expiryWarningThreshold
	 * @param checkLowStock
	 * @param checkExpired
	 * @param entity
	 * @param historyDetails
	 * @return Empty if nothing changed. Result if any state changed.
	 */
	private Optional<StoredExpiryLowStockProcessResult> getStoredExpiryLowStockProcessResult(
		String oqmDbIdOrName,
		ClientSession cs,
		Stored stored,
		Duration expiryWarningThreshold,
		boolean checkLowStock,
		boolean checkExpired,
		InteractingEntity entity,
		HistoryDetail... historyDetails
	) {
		StoredExpiryLowStockProcessResult curResult = StoredExpiryLowStockProcessResult.builder().storedId(stored.getId()).build();
		boolean changed = false;
		
		if (checkLowStock && stored.getType() == StoredType.AMOUNT) {
			AmountStored amountStored = (AmountStored) stored;
			
			if (UnitUtils.atOrUnderThreshold(amountStored.getLowStockThreshold(), amountStored.getAmount())) {
				curResult.setLowStock(true);
				if (!amountStored.getNotificationStatus().isLowStock()) {
					amountStored.getNotificationStatus().setLowStock(true);
					changed = true;
				}
			} else {
				curResult.setLowStock(false);
				if (amountStored.getNotificationStatus().isLowStock()) {
					amountStored.getNotificationStatus().setLowStock(false);
					changed = true;
				}
			}
		}
		
		if (checkExpired && stored.getExpires() != null) {
			ZonedDateTime now = ZonedDateTime.now();
			if (
				now.isAfter(stored.getExpires())
			) {
				//  2025-08-23T05:17:20.462707133 is after 2025-08-23T01:31
				log.info("{} ({}) is after {}", now, TimeZone.getDefault(), stored.getExpires());
				curResult.setExpired(true);
				curResult.setExpiryWarn(false);
				if (!stored.getNotificationStatus().isExpired()) {
					stored.getNotificationStatus().setExpired(true);
					stored.getNotificationStatus().setExpiredWarning(false);
					changed = true;
				}
			} else if (
					   !expiryWarningThreshold.equals(Duration.ZERO) &&
					   now.isAfter(stored.getExpires().minus(expiryWarningThreshold))
			) {
				curResult.setExpired(false);
				curResult.setExpiryWarn(true);
				if (!stored.getNotificationStatus().isExpiredWarning()) {
					stored.getNotificationStatus().setExpired(false);
					stored.getNotificationStatus().setExpiredWarning(true);
					changed = true;
				}
			} else {
				if(stored.getNotificationStatus().isExpiredWarning()){
					stored.getNotificationStatus().setExpiredWarning(false);
					changed = true;
				}
				if(stored.getNotificationStatus().isExpired()){
					stored.getNotificationStatus().setExpired(false);
					changed = true;
				}
			}
		}
		
		if (changed) {
			this.getStoredService().update(oqmDbIdOrName, cs, stored, entity, true, historyDetails);
			return Optional.of(curResult);
		}
		return Optional.empty();
	}
	
	/**
	 * Performs the necessary processing to recalculate stats about an item after an item has a transaction applied.
	 *
	 * If stats changed, updated inventory item.
	 *
	 * @param oqmDbIdOrName
	 * @param cs
	 * @param item
	 * @param transactionId
	 * @param concerning
	 * @param entity
	 * @param historyDetails
	 *
	 * @return
	 */
	public ItemPostTransactionProcessResults postTransactionProcess(
		String oqmDbIdOrName,
		ClientSession cs,
		InventoryItem item,
		ObjectId transactionId,
		Set<Stored> concerning,
		InteractingEntity entity,
		HistoryDetail... historyDetails
	) {
		//TODO:: apply mutex here?
		
		Set<ObjectId> concerningIds = concerning.stream().map(Stored::getId).collect(Collectors.toSet());
		//TODO:: separate thread to get these stats
		
		//process expiry and low stock for affected stored
		ItemExpiryLowStockItemProcessResults results = new ItemExpiryLowStockItemProcessResults().setItem(item.getId());
		{
			FindIterable<Stored> storedInItem = this.getStoredService().listIterator(
				oqmDbIdOrName, cs, new StoredSearch()
									   .setInventoryItemId(item.getId().toHexString())
									   .setInStorageBlocks(concerning.stream().map(Stored::getStorageBlock).distinct().collect(Collectors.toList()))
			);
			try (
				MongoCursor<Stored> storedIterator = storedInItem.iterator();
			) {
				while (storedIterator.hasNext()) {
					Stored curStored = storedIterator.next();
					
					Optional<StoredExpiryLowStockProcessResult> result = this.getStoredExpiryLowStockProcessResult(
						oqmDbIdOrName,
						cs,
						curStored,
						item.getExpiryWarningThreshold(),
						true,
						concerningIds.contains(curStored.getId()),
						entity,
						historyDetails
					);
					if (result.isPresent()) {
						StoredExpiryLowStockProcessResult curResult = result.get();
						
						if (!results.getResults().containsKey(curStored.getStorageBlock())) {
							results.getResults().put(curStored.getStorageBlock(), new ArrayList<>());
						}
						results.getResults().get(curStored.getStorageBlock()).add(curResult);
					}
				}
			}
		}
		
		ItemStoredStats oldStats = item.getStats();
		ItemStoredStats storedStats = this.getItemStats(oqmDbIdOrName, cs, item.getId());
		item.setStats(storedStats);
		
		boolean changed = false;
		
		if (!storedStats.equals(oldStats)) {
			changed = true;
		}
		if (item.getLowStockThreshold() != null) {
			if (UnitUtils.atOrUnderThreshold(item.getLowStockThreshold(), storedStats.getTotal())) {
				results.setLowStock(true);
				storedStats.setLowStock(true);
				
				if (!oldStats.isLowStock()) {
					changed = true;
				}
				
				if(!item.getNotificationStatus().isLowStock()) {
					item.getNotificationStatus().setLowStock(true);
					//TODO:: handle notification
				}
			} else {
				item.getNotificationStatus().setLowStock(false);
				storedStats.setLowStock(false);
				item.getNotificationStatus().setLowStock(false);
				
				if (oldStats.isLowStock()) {
					changed = true;
				}
			}
		}
		
		List<StoredExpiryLowStockProcessResult> inBlockResults = results.getResults().values().stream().flatMap(List::stream).toList();
		if (!inBlockResults.isEmpty()) {
			changed = true;
		}
		
		if (changed) {
			this.getInventoryItemService().update(oqmDbIdOrName, cs, item, entity, true, historyDetails);
			results.getEvents(transactionId).parallelStream().forEach(event->{
				if (event.getObjectId().equals(item.getId())) {
					this.getInventoryItemService().addHistoryFor(oqmDbIdOrName, cs, item, this.getCoreApiInteractingEntity(), event);
				} else {
					this.getStoredService().addHistoryFor(oqmDbIdOrName, cs, event.getObjectId(), this.getCoreApiInteractingEntity(), event);
				}
			});
			
		}
		
		return ItemPostTransactionProcessResults.builder()
				   .expiryLowStockResults(results)
				   .stats(storedStats)
				   .build();
	}
	
	/**
	 * Scans the entire given database for expired items.
	 * @param oqmDbIdOrName The id or name of the database to scan.
	 * @return The number of new
	 */
	public long scanForExpired(String oqmDbIdOrName) {
		try (MongoSessionWrapper csw = new MongoSessionWrapper(this.getStoredService())) {
			FindIterable<Stored> storedInItem = this.getStoredService().listIterator(
				oqmDbIdOrName,
				csw.getClientSession(),
				new StoredSearch()
					.setHasExpiryDate(true)
			);
			try (
				MongoCursor<Stored> storedIterator = storedInItem.iterator();
			) {
				Map<ObjectId, Duration> itemExpiryWarningThresholds = new HashMap<>();
				long output = 0L;
				while (storedIterator.hasNext()) {
					Stored curStored = storedIterator.next();
					
					if (!itemExpiryWarningThresholds.containsKey(curStored.getItem())) {
						itemExpiryWarningThresholds.put(
							curStored.getItem(),
							this.inventoryItemService.get(oqmDbIdOrName, curStored.getItem()).getExpiryWarningThreshold()
						);
					}
					
					Optional<StoredExpiryLowStockProcessResult> result = this.getStoredExpiryLowStockProcessResult(
						oqmDbIdOrName,
						csw.getClientSession(),
						curStored,
						itemExpiryWarningThresholds.get(curStored.getItem()),
						false,
						true,
						this.coreApiInteractingEntity
					);
					if (result.isPresent()) {
						StoredExpiryLowStockProcessResult curResult = result.get();
						
						for (
							ObjectHistoryEvent curEvent :
							curResult.getEvents(null, null)//Shouldn't hit the code that uses these parameters
						) {
							output++;
							this.getStoredService().addHistoryFor(oqmDbIdOrName, csw.getClientSession(), curStored, this.getCoreApiInteractingEntity(), curEvent);
						}
					}
				}
				//TODO:: update item stats
				return output;
			}
		}
	}
	
}
