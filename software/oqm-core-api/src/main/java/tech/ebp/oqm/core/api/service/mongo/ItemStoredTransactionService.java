package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinFullTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinPartTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferWholeTransaction;
import tech.ebp.oqm.core.api.model.rest.search.AppliedTransactionSearch;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.units.indriya.quantity.Quantities;

@Slf4j
@Named("ItemStoredTransactionService")
@ApplicationScoped
public class ItemStoredTransactionService extends MongoObjectService<AppliedTransaction, AppliedTransactionSearch, CollectionStats> {

	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	StoredService storedService;

	public ItemStoredTransactionService() {
		super(AppliedTransaction.class);
	}


	public ObjectId apply(
		String oqmDbIdOrName,
		ClientSession cs,
		@NotNull InventoryItem inventoryItem,
		@Valid ItemStoredTransaction itemStoredTransaction,
		InteractingEntity interactingEntity
	){
		//TODO:: if cs null, create.

		AppliedTransaction.Builder<?,?> appliedTransactionBuilder = AppliedTransaction.builder()
			.inventoryItem(inventoryItem.getId())
			.transaction(itemStoredTransaction);

		switch(itemStoredTransaction.getTransactionType()){
			case ADD_AMOUNT -> {
				AddAmountTransaction addAmountTransaction = (AddAmountTransaction) itemStoredTransaction;
				AmountStored stored;
				switch(inventoryItem.getStorageType()){
					case BULK -> {
						try{
							stored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), addAmountTransaction.getToBlock(), AmountStored.class);
						} catch (DbNotFoundException e){
							stored = AmountStored.builder()
								.item(inventoryItem.getId())
								.storageBlock(addAmountTransaction.getToBlock())
								.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
								.build();
							this.storedService.add(oqmDbIdOrName, cs, stored, interactingEntity);
						}
					}
					case AMOUNT_LIST -> {
						if(addAmountTransaction.getToStored() == null){
							stored = AmountStored.builder()
								.item(inventoryItem.getId())
								.storageBlock(addAmountTransaction.getToBlock())
								.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
								.build();
							this.storedService.add(oqmDbIdOrName, cs, stored, interactingEntity);
						} else {
							stored = (AmountStored) this.storedService.get(oqmDbIdOrName, cs, addAmountTransaction.getToStored());
						}
					}
					default -> {
						throw new IllegalArgumentException("Cannot add an amount to a unique item.");
					}
				}
				appliedTransactionBuilder.affectedStored(stored.getId());
				stored.add(addAmountTransaction.getAmount());

				this.storedService.update(oqmDbIdOrName, cs, stored, interactingEntity, null);//TODO:: new event type; applied transaction
			}
			case ADD_WHOLE -> {
				AddWholeTransaction addWholeTransaction = (AddWholeTransaction) itemStoredTransaction;
				Stored stored = addWholeTransaction.getToAdd();

				this.storedService.add(oqmDbIdOrName, cs, stored, interactingEntity);//TODO:: event details
			}
			case CHECKIN_FULL -> {
				CheckinFullTransaction cfTransaction = (CheckinFullTransaction) itemStoredTransaction;
				//TODO
			}
			case CHECKIN_PART -> {
				CheckinPartTransaction checkinPartTransaction = (CheckinPartTransaction) itemStoredTransaction;
				//TODO
			}
			case CHECKOUT_AMOUNT -> {
				CheckoutAmountTransaction checkoutAmountTransaction = (CheckoutAmountTransaction) itemStoredTransaction;
				//TODO
			}
			case CHECKOUT_WHOLE -> {
				CheckoutWholeTransaction cwTransaction = (CheckoutWholeTransaction) itemStoredTransaction;
				//TODO
			}
			case SUBTRACT_AMOUNT -> {
				SubAmountTransaction subAmountTransaction = (SubAmountTransaction) itemStoredTransaction;
				//TODO
			}
			case SUBTRACT_WHOLE -> {
				SubWholeTransaction subWholeTransaction = (SubWholeTransaction) itemStoredTransaction;
				ObjectId toSubtract = subWholeTransaction.getToSubtract();

				this.storedService.remove(oqmDbIdOrName, cs, toSubtract);//TODO:: event details
			}
			case TRANSFER_AMOUNT -> {
				TransferAmountTransaction transferAmountTransaction = (TransferAmountTransaction) itemStoredTransaction;
				//TODO
			}
			case TRANSFER_WHOLE -> {
				TransferWholeTransaction transferWholeTransaction = (TransferWholeTransaction) itemStoredTransaction;
				ObjectId toTransferId = transferWholeTransaction.getStoredToTransfer();

				Stored toTransfer = this.storedService.get(oqmDbIdOrName, cs, toTransferId);
				if(!transferWholeTransaction.getFromBlock().equals(toTransfer.getStorageBlock())){
					throw new IllegalArgumentException("Stored to transfer not starting out in expecting block.");
				}
				toTransfer.setStorageBlock(transferWholeTransaction.getToBlock());

				this.storedService.update(oqmDbIdOrName, cs, toTransfer, interactingEntity, null);//TODO:: event
			}
		}

		appliedTransactionBuilder.statsAfterApply(this.storedService.getItemStats(oqmDbIdOrName, cs, inventoryItem));

		ObjectId newId = this.add(oqmDbIdOrName, appliedTransactionBuilder.build());
		return newId;
	}

	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return null;
	}
}
