package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.receive;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
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
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveAmountInTransitTransaction;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitTransactionApplier;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.util.Set;

/**
 * Applier to handle AddAmountTransactions.
 *
 */
@ApplicationScoped
public class ReceiveAmountInTransitTransactionApplier extends InTransitTransactionApplier<ReceiveAmountInTransitTransaction> {

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.RECEIVE_AMOUNT_IN_TRANSIT;
	}

	private ObjectId getStoredIdForItemAndBlock(
		String oqmDbIdOrName,
		ClientSession cs,
		InventoryItem inventoryItem,
		ObjectId storageBlock,
		AmountStored inTransit
	) {
		Stored output = null;

		if(inventoryItem.getStorageType() == StorageType.BULK){
			try {
				SearchResult<Stored> blockSearch = this.getStoredService().getStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), storageBlock);
				output = blockSearch.getResults().getFirst();
			} catch(DbNotFoundException e) {
				//nothing to do
			}
		}

		if(output == null){
			output = AmountStored.builder()
						 .item(inventoryItem.getId())
						 .amount(Quantities.getQuantity(0, inTransit.getAmount().getUnit()))
						 .state(StoredInBlock.builder().storageBlock(storageBlock).build())
						 .build();
			this.getStoredService().add(oqmDbIdOrName, cs, output);
		}

		return output.getId();
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		ReceiveAmountInTransitTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		Stored inTransit = this.getAndAssertInTransitStored(oqmDbIdOrName, cs, inventoryItem, transaction.getInTransitStored());

		if (inTransit.getType() != StoredType.AMOUNT) {
			throw new IllegalArgumentException("In transit stored must be an amount type to receive an amount of it.");
		}
		AmountStored inTransitAmountStored = (AmountStored) inTransit;

		Quantity inTransitAmount = inTransitAmountStored.getAmount();
		Quantity transactionAmount = transaction.getAmount();

		boolean all = false;
		Quantity<?> toReceive;

		if (transaction.isAll()) {
			all = true;
			toReceive = inTransitAmount;
		} else {
			toReceive = transaction.getAmount();
			if(
				inTransitAmount.isEquivalentTo(transactionAmount)
			){
				all = true;
			}
		}

		if (toReceive == null) {
			throw new IllegalArgumentException("No amount specified to add.");
		}
		if (!toReceive.getUnit().isCompatible(inventoryItem.getUnit())) {
			throw new IllegalArgumentException("Unit of given amount not compatible with item.");
		}
		if (UnitUtils.isZero(toReceive)) {
			throw new IllegalArgumentException("Cannot add a zero amount.");
		}
		if (transaction.getToStored() != null && transaction.getToBlock() != null) {
			throw new IllegalArgumentException("Cannot specify both to stored and to block.");
		}



		ObjectId receivingId = null;
		{
			//from transaction
			if (transaction.getToStored() == null) {
				if (transaction.getToBlock() != null) {
					receivingId = this.getStoredIdForItemAndBlock(
						oqmDbIdOrName,
						cs,
						inventoryItem,
						transaction.getToBlock(),
						inTransitAmountStored
					);
				}
			} else {
				receivingId = transaction.getToStored();
			}

			//from established "to" fields
			InTransitLocale toLocale = ((InTransit) inTransit.getState()).getTo();
			if (receivingId == null && toLocale != null) {


				receivingId = switch (toLocale.getType()) {
					case STORAGE_BLOCK -> this.getStoredIdForItemAndBlock(
						oqmDbIdOrName,
						cs,
						inventoryItem,
						((StorageBlockLocale)toLocale).getStorageBlock(),
						inTransitAmountStored
					);
					case STORED -> ((StoredLocale)toLocale).getStored();
					case GENERIC -> null;
				};
			}

			if (receivingId == null) {
				throw new IllegalArgumentException("Where to receive the item ('to') could not be determined.");
			}
		}

		if(receivingId.equals(inTransit.getId())){
			throw new IllegalArgumentException("Cannot receive to same stored.");
		}

		AmountStored receiving = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, receivingId);

		if(receiving.getState().getType() != StoredStateType.STORED){
			throw new IllegalArgumentException("Cannot receive to non-stored item.");
		}

		receiving.add(toReceive);
		inTransitAmountStored.subtract(toReceive);

		if(inTransitAmountStored.getAmount().getValue().doubleValue() < 0){
			throw new IllegalArgumentException("Cannot receive more than is in transit.");
		}

		affectedStored.add(inTransit);
		affectedStored.add(receiving);

		this.getStoredService().update(oqmDbIdOrName, cs, receiving, interactingEntity, historyDetails);
		if(all){
			this.getStoredService().remove(oqmDbIdOrName, cs, inTransit.getId(), interactingEntity, historyDetails);
		} else {
			this.getStoredService().update(oqmDbIdOrName, cs, inTransit, interactingEntity, historyDetails);
		}

	}
}
