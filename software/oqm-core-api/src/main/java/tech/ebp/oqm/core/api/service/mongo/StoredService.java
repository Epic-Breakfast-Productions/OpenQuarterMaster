package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.processing.ItemExpiryLowStockItemProcessResults;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.processing.ItemPostTransactionProcessResults;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.processing.StoredExpiryLowStockProcessResult;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.*;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.*;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.search.ItemAwareSearchResult;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

import javax.measure.Quantity;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static tech.ebp.oqm.core.api.model.object.storage.items.StorageType.*;

@Named("StoredService")
@Slf4j
@ApplicationScoped
public class StoredService extends MongoHistoriedObjectService<Stored, StoredSearch, CollectionStats> {

	@Inject
	@Getter(AccessLevel.PRIVATE)
	CoreApiInteractingEntity coreApiInteractingEntity;

	@Inject
	@Getter(AccessLevel.PRIVATE)
	InventoryItemService inventoryItemService;

	@Inject
	@Getter(AccessLevel.PRIVATE)
	ItemCheckoutService itemCheckoutService;

	@Getter(AccessLevel.PRIVATE)
	HistoryEventNotificationService hens;


	@Override
	public Set<String> getDisallowedUpdateFields() {
		Set<String> output = new HashSet<>(super.getDisallowedUpdateFields());
		output.add("amount");
		output.add("item");
		output.add("storageBlock");
		return output;
	}

	public StoredService() {
		super(Stored.class, false);
		try (InstanceHandle<HistoryEventNotificationService> container = Arc.container().instance(HistoryEventNotificationService.class)) {
			this.hens = container.get();
		}
	}

	@WithSpan
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, Stored newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(oqmDbIdOrName, newObject, newOrChangedObject, clientSession);

		InventoryItem item;
		try {
			item = this.inventoryItemService.get(oqmDbIdOrName, newOrChangedObject.getItem());
		} catch (DbNotFoundException e) {
			throw new ValidationException("Item " + newOrChangedObject.getItem().toHexString() + " does not exist.", e);
		}

		if (!item.getStorageBlocks().contains(newOrChangedObject.getStorageBlock())) {
			throw new ValidationException("Storage block " + newOrChangedObject.getStorageBlock().toHexString() + " not used to hold this item.");
		}

		if (item.getStorageType().storedType != newOrChangedObject.getType()) {
			throw new ValidationException("Stored given of type " + newOrChangedObject.getType() + " cannot be held in item of storage type" + item.getStorageType());
		}

		if (item.getStorageType().storedType == StoredType.AMOUNT) {
			if (!item.getUnit().isCompatible(((AmountStored) newOrChangedObject).getAmount().getUnit())) {
				throw new ValidationException("Unit of amount must be compatible with item's unit.");
			}
		}

		if (item.getStorageType() == BULK || item.getStorageType() == UNIQUE_SINGLE) {
			SearchResult<Stored> inBlock = this.search(oqmDbIdOrName,
				new StoredSearch()
					.setInventoryItemId(item.getId().toHexString())
					.setStorageBlockId(newOrChangedObject.getStorageBlock().toHexString())
			);

			if (!inBlock.isEmpty()) {
				if (inBlock.getNumResults() != 1) {
					throw new ValidationException("More than one stored held for item of type " + item.getStorageType());
				}
				Stored stored = inBlock.getResults().get(0);
				if (newObject || !stored.getId().equals(newOrChangedObject.getId())) {
					throw new ValidationException("Cannot add more than one stored held for type " + item.getStorageType());
				}
			}
		}

		if (item.getStorageType() == UNIQUE_SINGLE) {
			SearchResult<Stored> inItem = this.search(oqmDbIdOrName, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
			if (!inItem.isEmpty()) {
				if (inItem.getNumResults() != 1) {
					throw new ValidationException("More than one globally unique stored held");
				}

				Stored stored = inItem.getResults().get(0);
				if (newObject || !stored.getId().equals(newOrChangedObject.getId())) {
					throw new ValidationException("Cannot store more than one globally unique stored item.");
				}
			}
		}
	}

	@Override
	public SearchResult<Stored> search(String oqmDbIdOrName, ClientSession cs, @NonNull StoredSearch searchObject) {
		SearchResult<Stored> results = super.search(oqmDbIdOrName, cs, searchObject);

		if(searchObject.getInventoryItemId() != null){
			results = new ItemAwareSearchResult<>(this.getInventoryItemService().get(oqmDbIdOrName, cs, new ObjectId(searchObject.getInventoryItemId())), results);
		}

		return results;
	}

	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, CollectionStats.builder())
			.build();
	}

	public <T extends Stored> SearchResult<T> getStoredForItemBlock(String oqmDbIdOrName, ClientSession cs, ObjectId itemId, ObjectId storageBlockId, Class<T> type) {
		SearchResult<T> result = (SearchResult<T>) this.search(oqmDbIdOrName, cs, new StoredSearch().setInventoryItemId(itemId.toHexString()).setStorageBlockId(storageBlockId.toHexString()));

		if (result.isEmpty()) {
			throw new DbNotFoundException("No stored currently stored in this block (" + storageBlockId + ") under this item (" + itemId + ").", this.clazz);
		}

		return result;
	}

	public SearchResult<Stored> getStoredForItemBlock(String oqmDbIdOrName, ClientSession cs, ObjectId itemId, ObjectId storageBlockId) {
		return this.getStoredForItemBlock(oqmDbIdOrName, cs, itemId, storageBlockId, Stored.class);
	}

	public <T extends Stored> T getSingleStoredForItemBlock(String oqmDbIdOrName, ClientSession cs, ObjectId itemId, ObjectId storageBlockId, Class<T> type) {
		SearchResult<T> result = this.getStoredForItemBlock(oqmDbIdOrName, cs, itemId, storageBlockId, type);

		if (result.getNumResults() != 1) {
			throw new IllegalStateException("Expected single stored in this block (" + storageBlockId + ") under this item (" + itemId + "), but got " + result.getNumResults() + ".");
		}

		return result.getResults().getFirst();
	}


	private void addToStats(BasicStatsContaining statsToAddTo, Stored stored) {

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
	}

	private void addToStats(StatsWithTotalContaining statsToAddTo, Stored stored) {
		this.addToStats((BasicStatsContaining) statsToAddTo, stored);

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

	private void addToStats(StoredInBlockStats storedInBlockStats, Stored stored) {
		storedInBlockStats.setHasStored(true);
		this.addToStats((StatsWithTotalContaining) storedInBlockStats, stored);
	}

	private void addToStats(ItemStoredStats itemStoredStats, Stored stored) {
		StoredInBlockStats storedInBlockStats = itemStoredStats.getStorageBlockStats().get(stored.getStorageBlock());

		this.addToStats(storedInBlockStats, stored);
		this.addToStats((StatsWithTotalContaining) itemStoredStats, stored);
	}

	private void addToStats(String oqmDbIdOrName, ClientSession cs, StoredStats storedStats, Stored stored) {

		if (!storedStats.getItemStats().containsKey(stored.getId())) {
			storedStats.getItemStats().put(stored.getId(), new ItemStoredStats(
				this.inventoryItemService.get(oqmDbIdOrName, cs, stored.getItem()).getUnit()
			));
		}
		ItemStoredStats itemStoredStats = storedStats.getItemStats().get(stored.getItem());

		this.addToStats((BasicStatsContaining) storedStats, stored);
		this.addToStats(itemStoredStats, stored);
	}

	public StoredStats getStoredStats(String oqmDbIdOrName, ClientSession cs, StoredSearch search) {
		FindIterable<Stored> storedInItem = this.listIterator(oqmDbIdOrName, cs, search);
		StoredStats output = new StoredStats();

		try (
			MongoCursor<Stored> storedIterator = storedInItem.iterator()
		) {
			while (storedIterator.hasNext()) {
				Stored curStored = storedIterator.next();

				this.addToStats(
					oqmDbIdOrName,
					cs,
					output,
					curStored
				);
			}
		}

		return output;
	}

	public ItemStoredStats getItemStats(String oqmDbIdOrName, ClientSession cs, ObjectId itemId) {
		FindIterable<Stored> storedInItem = this.listIterator(oqmDbIdOrName, cs, new StoredSearch().setInventoryItemId(itemId.toHexString()));

		InventoryItem item = this.inventoryItemService.get(oqmDbIdOrName, cs, itemId);
		ItemStoredStats output = new ItemStoredStats(item.getUnit());

		for (ObjectId storageBlock : item.getStorageBlocks()){
			output.getStorageBlockStats().put(storageBlock, new StoredInBlockStats(output.getTotal().getUnit()));
		}

		try (
			MongoCursor<Stored> storedIterator = storedInItem.iterator()
		) {
			while (storedIterator.hasNext()) {
				Stored curStored = storedIterator.next();

				this.addToStats(
					output,
					curStored
				);
			}
		}

		return output;
	}

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
		StoredExpiryLowStockProcessResult curResult = new StoredExpiryLowStockProcessResult();
		boolean changed = false;

		if (checkLowStock && stored.getType() == StoredType.AMOUNT) {
			AmountStored amountStored = (AmountStored) stored;

			if (UnitUtils.underThreshold(amountStored.getLowStockThreshold(), amountStored.getAmount())) {
				if (!amountStored.getNotificationStatus().isLowStock()) {
					amountStored.getNotificationStatus().setLowStock(true);
					curResult.setLowStock(true);
					changed = true;
				}
			} else {
				if (amountStored.getNotificationStatus().isLowStock()) {
					amountStored.getNotificationStatus().setLowStock(false);
					changed = true;
				}
			}
		}

		if (checkExpired && stored.getExpires() != null) {
			if (stored.getExpires().isBefore(LocalDateTime.now())) {
				if (!stored.getNotificationStatus().isExpired()) {
					changed = true;
					stored.getNotificationStatus().setExpired(true);
					stored.getNotificationStatus().setExpiredWarning(false);
					curResult.setExpired(true);
				}
			} else if (
				!expiryWarningThreshold.equals(Duration.ZERO) &&
					stored.getExpires().isBefore(LocalDateTime.now().plus(expiryWarningThreshold))
			) {
				if (!stored.getNotificationStatus().isExpiredWarning()) {
					changed = true;
					stored.getNotificationStatus().setExpired(false);
					stored.getNotificationStatus().setExpiredWarning(true);
					curResult.setExpiryWarn(true);
				}
			}
		}

		if (changed) {
			this.update(oqmDbIdOrName, cs, stored, entity, historyDetails);
			return Optional.of(curResult);
		}
		return Optional.empty();
	}

	/**
	 * @param oqmDbIdOrName
	 * @param cs
	 * @param item
	 * @param transactionId
	 * @param concerning
	 * @param entity
	 * @param historyDetails
	 * @return
	 */
	public ItemPostTransactionProcessResults postTransactionProcess(
		String oqmDbIdOrName,
		ClientSession cs,
		InventoryItem item, ObjectId transactionId, Set<Stored> concerning, InteractingEntity entity, HistoryDetail... historyDetails
	) {
		//TODO:: apply mutex here?

		Set<ObjectId> concerningIds = concerning.stream().map(Stored::getId).collect(Collectors.toSet());
		//TODO:: separate thread to get these stats

		//process expiry and low stock for affected stored
		ItemExpiryLowStockItemProcessResults results = new ItemExpiryLowStockItemProcessResults().setItemId(item.getId());
		{
			FindIterable<Stored> storedInItem = this.listIterator(
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
						entity, historyDetails
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

		if(!storedStats.equals(oldStats)){
			changed = true;
		}
		if (item.getLowStockThreshold() != null) {
			if (UnitUtils.underThreshold(item.getLowStockThreshold(), storedStats.getTotal())) {
				if (!item.getNotificationStatus().isLowStock()) {
					changed = true;
					item.getNotificationStatus().setLowStock(true);
					results.setLowStock(true);
				}
			} else {
				if (item.getNotificationStatus().isLowStock()) {
					changed = true;
					item.getNotificationStatus().setLowStock(false);
				}
			}
		}

		List<StoredExpiryLowStockProcessResult> inBlockResults = results.getResults().values().stream().flatMap(List::stream).toList();
		if(!inBlockResults.isEmpty()){
			changed = true;
		}

		if (changed) {
			this.getInventoryItemService().update(oqmDbIdOrName, cs, item, entity, historyDetails);
			results.getEvents(transactionId).parallelStream().forEach(event -> {
				if(event.getObjectId().equals(item.getId())){
					this.getInventoryItemService().addHistoryFor(oqmDbIdOrName, cs, item, this.getCoreApiInteractingEntity(), event);
				} else {
					this.addHistoryFor(oqmDbIdOrName, cs, event.getObjectId(), this.getCoreApiInteractingEntity(), event);
				}
			});
		}

		return ItemPostTransactionProcessResults.builder()
			.expiryLowStockResults(results)
			.stats(storedStats)
			.build();
	}

	public long scanForExpired(String oqmDbIdOrName){
		try (MongoSessionWrapper csw = new MongoSessionWrapper(this)) {
			FindIterable<Stored> storedInItem = this.listIterator(
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

					if(!itemExpiryWarningThresholds.containsKey(curStored.getItem())){
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
						true,
						false,
						this.coreApiInteractingEntity
					);
					if (result.isPresent()) {
						StoredExpiryLowStockProcessResult curResult = result.get();

						for(
							ObjectHistoryEvent curEvent :
							curResult.getEvents(null, null)//Shouldn't hit the code that uses these parameters
							){
							output++;
							this.addHistoryFor(oqmDbIdOrName, csw.getClientSession(), curStored, this.getCoreApiInteractingEntity(), curEvent);
						}
					}
				}
				//TODO:: update item stats
				return output;
			}
		}
	}

//TODO:: get referencing....
}