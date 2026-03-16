package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set.SetAmountTransaction;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.units.indriya.quantity.Quantities;

import java.util.Set;

public class SetAmountTransactionApplier extends TransactionApplier<SetAmountTransaction> {

	public SetAmountTransactionApplier(StoredService storedService) {
		super(storedService);
	}

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.SET_AMOUNT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		SetAmountTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		AmountStored stored;
		switch (inventoryItem.getStorageType()) {
			case BULK -> {
				if (transaction.getBlock() != null) {
					try {
						stored = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transaction.getBlock(), AmountStored.class);
					} catch (DbNotFoundException e) {
						stored = AmountStored.builder()
							.item(inventoryItem.getId())
							.storageBlock(transaction.getBlock())
							.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
							.build();
						this.getStoredService().add(oqmDbIdOrName, cs, stored, interactingEntity);
					}
					if (transaction.getStored() != null) {
						if (!stored.getId().equals(transaction.getStored())) {
							throw new IllegalArgumentException("To Stored given does not match stored found in block.");
						}
					}
				} else if (transaction.getStored() != null) {
					stored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getStored());
				} else {
					throw new IllegalArgumentException("No to block or stored given.");
				}
			}
			case AMOUNT_LIST -> {
				if (transaction.getStored() == null) {
					throw new IllegalArgumentException("Must specify a stored to set the amount of.");
				} else {
					stored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getStored());
					if (transaction.getBlock() != null && !stored.getStorageBlock().equals(transaction.getBlock())) {
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

		stored.setAmount(transaction.getAmount());

		affectedStored.add(stored);
		this.getStoredService().update(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
	}
}
