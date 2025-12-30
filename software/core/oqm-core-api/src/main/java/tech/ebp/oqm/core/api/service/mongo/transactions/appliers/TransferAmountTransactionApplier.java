package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.util.Set;

public class TransferAmountTransactionApplier extends TransactionApplier<TransferAmountTransaction> {
	
	public TransferAmountTransactionApplier(StoredService storedService) {
		super(storedService);
	}
	
	@Override
	public TransactionType getTransactionType() {
		return TransactionType.TRANSFER_AMOUNT;
	}
	
	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		TransferAmountTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		AmountStored fromStored;
		AmountStored toStored;
		
		switch (inventoryItem.getStorageType()) {
			case BULK -> {
				fromStored = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transaction.getFromBlock(), AmountStored.class);
				try {
					toStored = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transaction.getToBlock(), AmountStored.class);
				} catch(DbNotFoundException e) {
					toStored = AmountStored.builder()
								   .item(inventoryItem.getId())
								   .storageBlock(transaction.getToBlock())
								   .amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
								   .build();
					this.getStoredService().add(oqmDbIdOrName, cs, toStored, interactingEntity, historyDetails);
				}
			}
			case AMOUNT_LIST -> {
				fromStored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getFromStored());
				if (transaction.getToStored() == null) {
					toStored = AmountStored.builder()
								   .item(inventoryItem.getId())
								   .storageBlock(transaction.getToBlock())
								   .amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
								   .build();
					this.getStoredService().add(oqmDbIdOrName, cs, toStored, interactingEntity);
				} else {
					toStored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getToStored());
				}
			}
			default -> {
				throw new IllegalArgumentException("Cannot subtract an amount from a unique item.");
			}
		}
		
		
		if (!inventoryItem.getId().equals(toStored.getItem())) {
			throw new IllegalArgumentException("To Stored is not associated with the item.");
		}
		if (!inventoryItem.getId().equals(fromStored.getItem())) {
			throw new IllegalArgumentException("From Stored is not associated with the item.");
		}
		
		if (transaction.getFromStored() != null && !transaction.getFromStored().equals(fromStored.getId())) {
			throw new IllegalArgumentException("From Stored retrieved not in specified block.");
		}
		if (transaction.getToStored() != null && !transaction.getToStored().equals(toStored.getId())) {
			throw new IllegalArgumentException("To Stored retrieved not in specified block.");
		}
		if(fromStored.equals(toStored) || fromStored.getId().equals(toStored.getId())){
			throw new IllegalArgumentException("Cannot transfer from/to the same Stored.");
		}
		if (transaction.getAmount() == null && !transaction.isAll()) {
			throw new IllegalArgumentException("Either amount or all is required.");
		}
		if (transaction.getAmount() != null && transaction.isAll()) {
			throw new IllegalArgumentException("Either amount or all is required, but not both.");
		}
		
		Quantity toTransfer = (
			transaction.isAll() ?
				fromStored.getAmount() :
				transaction.getAmount()
		);
		
		if(toTransfer.getValue().equals(0)){
			throw new IllegalArgumentException("Amount to transfer must be greater than zero.");
		}
		
		fromStored.subtract(toTransfer);
		toStored.add(toTransfer);
		
		affectedStored.add(toStored);
		affectedStored.add(fromStored);
		this.getStoredService().update(oqmDbIdOrName, cs, fromStored, interactingEntity, historyDetails);
		this.getStoredService().update(oqmDbIdOrName, cs, toStored, interactingEntity, historyDetails);
	}
}
