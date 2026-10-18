package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.storedToInTransit;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.InTransitLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StorageBlockLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StoredLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.storedToInTransit.StoredToInTransitAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.storedToInTransit.StoredToInTransitWholeTransaction;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitTransactionApplier;

import javax.measure.Quantity;
import java.util.Set;

/**
 * Applier to handle AddAmountTransactions.
 *
 */
@ApplicationScoped
public class StoredToInTransitAmountTransactionApplier extends InTransitTransactionApplier<StoredToInTransitAmountTransaction> {

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.STORED_TO_IN_TRANSIT_AMOUNT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		StoredToInTransitAmountTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		if(inventoryItem.getStorageType().storedType != StoredType.AMOUNT){
			throw new IllegalArgumentException("Cannot move amounts of unique items.");
		}

		if (transaction.getSubtractFromStored() == null && transaction.getSubtractFromBlock() == null) {
			throw new IllegalArgumentException("Must specify either from stored or from block.");
		}
		if (transaction.getSubtractFromStored() != null && transaction.getSubtractFromBlock() != null) {
			throw new IllegalArgumentException("Cannot specify both from stored and from block.");
		}


		AmountStored fromStored;
		{
			Stored stored;
			if (transaction.getSubtractFromStored() != null) {
				stored = this.getAndAssertStored(oqmDbIdOrName, cs, inventoryItem, transaction.getSubtractFromStored(), StoredStateType.STORED);
				if(stored.getType() != StoredType.AMOUNT){
					throw new IllegalArgumentException("Cannot move amounts of unique items.");
				}
				fromStored = (AmountStored) stored;
			} else {
				fromStored = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transaction.getSubtractFromBlock(), AmountStored.class);
			}
		}

		Quantity amountStoredAmount = fromStored.getAmount();
		Quantity transactionAmount = transaction.getAmount();

		boolean all = false;
		Quantity<?> toTransit;

		if (transaction.isAll()) {
			all = true;
			toTransit = amountStoredAmount;
		} else {
			toTransit = transactionAmount;
			if(
				amountStoredAmount.isEquivalentTo(transactionAmount)
			){
				all = true;
			}
		}

		if (toTransit == null) {
			throw new IllegalArgumentException("No amount specified to move to in transit.");
		}
		if (!toTransit.getUnit().isCompatible(inventoryItem.getUnit())) {
			throw new IllegalArgumentException("Unit of given amount not compatible with item.");
		}
		if (UnitUtils.isZero(toTransit)) {
			throw new IllegalArgumentException("Cannot move a zero amount.");
		}



		ObjectId receivingId = null;

		{
			if(transaction.getToInTransitStored() != null){
				receivingId = transaction.getToInTransitStored();
			} else {
				AmountStored newInTransit = fromStored.toBuilder()
												.state(InTransit.builder()
														   .from(StoredLocale.builder()
																	 .stored(fromStored.getId())
																	 .build())
														   .build())
												.build();
				newInTransit = (AmountStored) this.getStoredService().add(
					oqmDbIdOrName,
					cs,
					newInTransit,
					interactingEntity
				);
				receivingId = newInTransit.getId();
			}
		}

		if(receivingId.equals(fromStored.getId())){
			throw new IllegalArgumentException("Cannot move amount transit to same stored.");
		}

		AmountStored receiving = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, receivingId);

		if(receiving.getState().getType() != StoredStateType.IN_TRANSIT){
			throw new IllegalArgumentException("Cannot move amount to non-in transit stored.");
		}

		receiving.add(toTransit);
		fromStored.subtract(toTransit);

		if(fromStored.getAmount().getValue().doubleValue() < 0){
			throw new IllegalArgumentException("Cannot move to in-transit more than was stored.");
		}

		affectedStored.add(fromStored);
		affectedStored.add(receiving);

		this.getStoredService().update(oqmDbIdOrName, cs, receiving, interactingEntity, historyDetails);
		if(all){
			this.getStoredService().remove(oqmDbIdOrName, cs, fromStored.getId(), interactingEntity, historyDetails);
		} else {
			this.getStoredService().update(oqmDbIdOrName, cs, fromStored, interactingEntity, historyDetails);
		}
	}
}
