package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.service.mongo.StoredService;

import javax.measure.Quantity;
import java.util.Set;

public class SubtractAmountTransactionApplier extends TransactionApplier<SubAmountTransaction> {

	public SubtractAmountTransactionApplier(StoredService storedService) {
		super(storedService);
	}

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.SUBTRACT_AMOUNT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		SubAmountTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		AmountStored stored;
		switch (inventoryItem.getStorageType()) {
			case BULK -> {
				if(transaction.getFromBlock() != null){
					stored = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transaction.getFromBlock(), AmountStored.class);
				} else if (transaction.getFromStored() != null){
					stored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getFromStored());
				} else {
					throw new IllegalArgumentException("Must specify where the subtraction is happening, either the storage block or stored.");
				}
				
			}
			case AMOUNT_LIST -> {
				if(transaction.getFromStored() == null){
					throw new IllegalArgumentException("Must specify the stored item we are subtracting from.");
				}
				
				stored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getFromStored());
			}
			default -> {
				throw new IllegalArgumentException("Cannot subtract an amount from a unique item.");
			}
		}

		if (!inventoryItem.getId().equals(stored.getItem())) {
			throw new IllegalArgumentException("Stored is not associated with the item.");
		}

		if (transaction.getFromStored() != null && !stored.getId().equals(transaction.getFromStored())) {
			throw new IllegalArgumentException("Stored id in transaction not the id of stored found.");
		}
		if (transaction.getFromBlock() != null && !stored.getStorageBlock().equals(transaction.getFromBlock())) {
			throw new IllegalArgumentException("Stored retrieved not in specified block.");
		}
		if(
			(transaction.getAmount() == null && !transaction.isAll()) ||
			(transaction.getAmount() != null && transaction.isAll())
		){
			throw new IllegalArgumentException("Transaction must specify either to subtract an amount or all amount present.");
		}
		
		Quantity toSubtract = null;
		
		if(transaction.getAmount() != null) {
			toSubtract = transaction.getAmount();
		}
		if(transaction.isAll()) {
			toSubtract = stored.getAmount();
		}
		
		if(toSubtract == null){
			throw new IllegalStateException("Cannot subtract an amount from a null value. Should not get here.");
		}
		
		stored.subtract(toSubtract);

		affectedStored.add(stored);
		this.getStoredService().update(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
	}
}
