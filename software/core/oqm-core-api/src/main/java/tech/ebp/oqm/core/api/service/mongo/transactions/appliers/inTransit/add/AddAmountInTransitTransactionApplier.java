package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.add;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddAmountInTransitTransaction;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitTransactionApplier;

import java.util.Set;

/**
 * Applier to handle AddAmountTransactions.
 *
 */
@ApplicationScoped
public class AddAmountInTransitTransactionApplier extends InTransitTransactionApplier<AddAmountInTransitTransaction> {

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.ADD_AMOUNT_IN_TRANSIT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		AddAmountInTransitTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		if (inventoryItem.getStorageType().storedType != StoredType.AMOUNT) {
			throw new IllegalArgumentException("Item is not an amount holding type.");
		}
		this.assertInTransitValid(cs, oqmDbIdOrName, inventoryItem, transaction.getDetails());

		AmountStored stored = AmountStored.builder()
								  .item(inventoryItem.getId())
								  .amount(transaction.getAmount())
								  .state(transaction.getDetails())
								  .build();

		affectedStored.add(stored);
		this.getStoredService().add(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
	}
}
