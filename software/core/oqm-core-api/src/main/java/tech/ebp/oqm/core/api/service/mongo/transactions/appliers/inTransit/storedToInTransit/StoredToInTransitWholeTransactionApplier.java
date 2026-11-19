package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.storedToInTransit;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StorageBlockLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StoredLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.storedToInTransit.StoredToInTransitWholeTransaction;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitTransactionApplier;

import java.util.Set;

/**
 * Applier to handle AddAmountTransactions.
 *
 */
@ApplicationScoped
public class StoredToInTransitWholeTransactionApplier extends InTransitTransactionApplier<StoredToInTransitWholeTransaction> {

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.STORED_TO_IN_TRANSIT_WHOLE;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		StoredToInTransitWholeTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		if (inventoryItem.getStorageType() == StorageType.BULK) {
			throw new IllegalArgumentException("Cannot move whole stored in BULK types.");
		}

		Stored inTransit = this.getAndAssertStored(oqmDbIdOrName, cs, inventoryItem, transaction.getToTransit());

		if (inTransit.getState().getType() != StoredStateType.STORED) {
			throw new IllegalArgumentException("Target stored must be currently stored.");
		}

		InTransit newState = InTransit.builder()
								 .from(StorageBlockLocale.builder()
										   .storageBlock(
											   ((StoredInBlock) inTransit.getState()).getStorageBlock()
										   )
										   .build())
								 .to(transaction.getState().getTo())
								 .attributes(transaction.getState().getAttributes())
								 .keywords(transaction.getState().getKeywords())
								 .imageIds(transaction.getState().getImageIds())
								 .attachedFiles(transaction.getState().getAttachedFiles())
								 .build();
		inTransit.setState(newState);

		affectedStored.add(inTransit);
		this.getStoredService().update(oqmDbIdOrName, cs, inTransit, interactingEntity, historyDetails);
	}
}
