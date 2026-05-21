package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.units.indriya.quantity.Quantities;

import java.util.Set;

/**
 * Applier to handle AddAmountTransactions.
 *
 */
@ApplicationScoped
public class AddAmountTransactionApplier extends TransactionApplier<AddAmountTransaction> {

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.ADD_AMOUNT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		AddAmountTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		AmountStored stored;
		switch (inventoryItem.getStorageType()) {
			case BULK -> {
				if (transaction.getToBlock() != null) {
					try {
						stored = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transaction.getToBlock(), AmountStored.class);
					} catch(DbNotFoundException e) {
						stored = AmountStored.builder()
									 .item(inventoryItem.getId())
									 .state(StoredInBlock.builder().storageBlock(transaction.getToBlock()).build())
									 .amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
									 .build();
						this.getStoredService().add(oqmDbIdOrName, cs, stored, interactingEntity);
					}
					if (transaction.getToStored() != null) {
						if (!stored.getId().equals(transaction.getToStored())) {
							throw new IllegalArgumentException("To Stored given does not match stored found in block.");
						}
					}
				} else if (transaction.getToStored() != null) {
					stored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getToStored());
				} else {
					throw new IllegalArgumentException("No to block or stored given.");
				}
			}
			case AMOUNT_LIST -> {
				if (transaction.getToStored() == null) {
					if(transaction.getToBlock() == null){
						throw new IllegalArgumentException("Must specify a block or stored to add to.");
					}

					stored = AmountStored.builder()
								 .item(inventoryItem.getId())
								 .state(StoredInBlock.builder().storageBlock(transaction.getToBlock()).build())
								 .amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
								 .build();
					this.getStoredService().add(oqmDbIdOrName, cs, stored, interactingEntity);
				} else {
					stored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getToStored());

					if(!stored.isState(StoredStateType.STORED)){
						throw new IllegalArgumentException("Cannot add to stored that is not stored in a block.");
					}

					if (
						transaction.getToBlock() != null &&
						!((StoredInBlock)(stored.getState())).getStorageBlock().equals(transaction.getToBlock())
					) {
						throw new IllegalArgumentException("To Stored given does not exist in block.");
					}
				}
			}
			default -> {
				throw new IllegalArgumentException("Cannot add an amount to a unique item.");
			}
		}

		if (!inventoryItem.getId().equals(stored.getItem())) {
			throw new IllegalArgumentException("Stored is not associated with the item.");
		}

		if(!stored.isState(StoredStateType.STORED)){
			throw new IllegalArgumentException("Cannot add to stored that is not stored in a block.");
		}

		stored.add(transaction.getAmount());

		affectedStored.add(stored);
		this.getStoredService().update(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
	}
}
