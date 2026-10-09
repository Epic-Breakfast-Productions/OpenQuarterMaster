package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.receive;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveWholeInTransitTransaction;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitTransactionApplier;

import java.util.Set;

/**
 * Applier to handle AddAmountTransactions.
 *
 */
@ApplicationScoped
public class ReceiveWholeInTransitTransactionApplier extends InTransitTransactionApplier<ReceiveWholeInTransitTransaction> {

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.RECEIVE_AMOUNT_IN_TRANSIT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		ReceiveWholeInTransitTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		Stored inTransit = this.getStoredService().get(
			oqmDbIdOrName,
			cs,
			transaction.getInTransitStored()
		);

		//TODO:: do






//		if (inventoryItem.getStorageType().storedType != StoredType.AMOUNT) {
//			throw new IllegalArgumentException("Item is not an amount holding type.");
//		}
//		this.assertInTransitValid(cs, oqmDbIdOrName, inventoryItem, transaction.getDetails());
//
//		Stored stored = transaction.getToAdd();
//
//		if (!inventoryItem.getId().equals(stored.getItem())) {
//			throw new IllegalArgumentException("Stored item's associated item must match concerning item.");
//		}
//
//		stored.setState(transaction.getDetails());
//
//		affectedStored.add(stored);
//		this.getStoredService().add(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
	}
}
