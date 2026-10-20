package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.update;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.InTransitLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.ReturnableLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StorageBlockLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StoredLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.cancel.CancelInTransitWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.update.UpdateInTransitTransaction;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitTransactionApplier;

import java.util.List;
import java.util.Set;

/**
 * Applier to handle AddAmountTransactions.
 *
 */
@Slf4j
@ApplicationScoped
public class UpdateInTransitTransactionApplier extends InTransitTransactionApplier<UpdateInTransitTransaction> {

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.UPDATE_IN_TRANSIT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		UpdateInTransitTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		Stored stored = this.getAndAssertStored(oqmDbIdOrName, cs, inventoryItem, transaction.getInTransitStored(), StoredStateType.IN_TRANSIT);

		boolean updated = false;

		if(transaction.getAmount() != null){
			if(stored.getType() != StoredType.AMOUNT){
				throw new IllegalArgumentException("Cannot update amount in non-amount stored object");
			}

			AmountStored amountStored = (AmountStored) stored;

			if(!amountStored.getAmount().getUnit().isCompatible(transaction.getAmount().getUnit())){
				throw new IllegalArgumentException("Cannot update amount with different unit.");
			}

			amountStored.setAmount(transaction.getAmount());
			updated = true;
		}

		updated |= ((InTransit)stored.getState()).updateFromToTransitState(transaction.getInTransitUpdates());


		if(!updated){
			throw new IllegalArgumentException("Nothing given to update with.");
		}
		
		affectedStored.add(stored);
		this.getStoredService().update(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
	}
}
