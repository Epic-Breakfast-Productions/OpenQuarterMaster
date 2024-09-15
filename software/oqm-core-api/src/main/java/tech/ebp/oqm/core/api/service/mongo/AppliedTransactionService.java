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
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemAmountCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemWholeCheckout;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service to handle applying transactions to items stored, and keeping track of what transactions have been applied.
 */
@Slf4j
@Named("ItemStoredTransactionService")
@ApplicationScoped
public class AppliedTransactionService extends MongoObjectService<AppliedTransaction, AppliedTransactionSearch, CollectionStats> {

//	@Inject
//	InventoryItemService inventoryItemService;

	@Inject
	StoredService storedService;

	@Inject
	ItemCheckoutService itemCheckoutService;

	public AppliedTransactionService() {
		super(AppliedTransaction.class);
	}

	/**
	 * Applies the transaction given.
	 * @param oqmDbIdOrName
	 * @param cs
	 * @param inventoryItem
	 * @param itemStoredTransaction
	 * @param interactingEntity
	 * @return
	 */
	public ObjectId apply(
		String oqmDbIdOrName,
		ClientSession cs,
		@NotNull InventoryItem inventoryItem,
		@Valid ItemStoredTransaction itemStoredTransaction,
		InteractingEntity interactingEntity,
		HistoryDetail ... details
	){
		//TODO:: if cs null, create.
		final ObjectId transactionId = new ObjectId();
		HistoryDetail[] historyDetails;
		{
			List<HistoryDetail> deetsCollection = new ArrayList<>();
			deetsCollection.addAll(List.of(details));
			deetsCollection.add(new ItemTransactionDetail(transactionId));
			historyDetails = deetsCollection.toArray(new HistoryDetail[0]);
		}
		AppliedTransaction.Builder<?,?> appliedTransactionBuilder = AppliedTransaction.builder()
			.id(transactionId)
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
				appliedTransactionBuilder.affectedStored(Set.of(stored.getId()));
				stored.add(addAmountTransaction.getAmount());

				appliedTransactionBuilder.affectedStored(Set.of(stored.getId()));
				this.storedService.update(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
			}
			case ADD_WHOLE -> {
				AddWholeTransaction addWholeTransaction = (AddWholeTransaction) itemStoredTransaction;
				Stored stored = addWholeTransaction.getToAdd();

				appliedTransactionBuilder.affectedStored(Set.of(stored.getId()));
				this.storedService.add(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
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
				Stored affectedStored = this.storedService.get(oqmDbIdOrName, cwTransaction.getToCheckout());
				appliedTransactionBuilder.affectedStored(Set.of(affectedStored.getId()));
				ItemCheckout.Builder<?, ?, ?> checkoutBuilder;
				switch(inventoryItem.getStorageType()){
					case BULK -> {
						AmountStored affectedAmountStored = (AmountStored) affectedStored;
						checkoutBuilder = ItemAmountCheckout.builder()
							.checkedOut(affectedAmountStored.getAmount());
						affectedAmountStored.setAmount(Quantities.getQuantity(0, affectedAmountStored.getAmount().getUnit()));
						this.storedService.update(oqmDbIdOrName, cs, affectedAmountStored, interactingEntity, historyDetails);
					}
					case AMOUNT_LIST, UNIQUE_MULTI, UNIQUE_SINGLE -> {
						checkoutBuilder = ItemWholeCheckout.builder()
							.checkedOut(affectedStored);
						this.storedService.remove(oqmDbIdOrName, cs, affectedStored.getId(), interactingEntity, historyDetails);
					}
					default -> {
						throw new IllegalStateException("Storage type not supported. This should never happen.");
					}
				}
				checkoutBuilder.item(inventoryItem.getId())
					.checkedOutFrom(affectedStored.getStorageBlock())
					.checkoutDetails(cwTransaction.getCheckoutDetails())
					.transaction(transactionId)
					;
				this.itemCheckoutService.add(oqmDbIdOrName, cs, checkoutBuilder.build(), interactingEntity);
			}
			case SUBTRACT_AMOUNT -> {
				SubAmountTransaction subAmountTransaction = (SubAmountTransaction) itemStoredTransaction;
				AmountStored stored;
				switch(inventoryItem.getStorageType()){
					case BULK -> {
						stored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), subAmountTransaction.getFromBlock(), AmountStored.class);
					}
					case AMOUNT_LIST -> {
						stored = (AmountStored) this.storedService.get(oqmDbIdOrName, cs, subAmountTransaction.getFromStored());
					}
					default -> {
						throw new IllegalArgumentException("Cannot subtract an amount from a unique item.");
					}
				}
				stored.subtract(subAmountTransaction.getAmount());

				appliedTransactionBuilder.affectedStored(Set.of(stored.getId()));
				this.storedService.update(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
			}
			case SUBTRACT_WHOLE -> {
				SubWholeTransaction subWholeTransaction = (SubWholeTransaction) itemStoredTransaction;
				ObjectId toSubtract = subWholeTransaction.getToSubtract();

				appliedTransactionBuilder.affectedStored(Set.of(toSubtract));
				this.storedService.remove(oqmDbIdOrName, cs, toSubtract, interactingEntity, historyDetails);
			}
			case TRANSFER_AMOUNT -> {
				TransferAmountTransaction transferAmountTransaction = (TransferAmountTransaction) itemStoredTransaction;
				AmountStored fromStored;
				AmountStored toStored;

				switch(inventoryItem.getStorageType()){
					case BULK -> {
						fromStored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transferAmountTransaction.getFromBlock(), AmountStored.class);
						try{
							toStored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transferAmountTransaction.getToBlock(), AmountStored.class);
						} catch (DbNotFoundException e){
							toStored = AmountStored.builder()
								.item(inventoryItem.getId())
								.storageBlock(transferAmountTransaction.getToBlock())
								.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
								.build();
							this.storedService.add(oqmDbIdOrName, cs, toStored, interactingEntity);
						}
					}
					case AMOUNT_LIST -> {
						fromStored = (AmountStored) this.storedService.get(oqmDbIdOrName, cs, transferAmountTransaction.getFromStored());
						if(transferAmountTransaction.getToStored() == null){
							toStored = AmountStored.builder()
								.item(inventoryItem.getId())
								.storageBlock(transferAmountTransaction.getToBlock())
								.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
								.build();
							this.storedService.add(oqmDbIdOrName, cs, toStored, interactingEntity);
						} else {
							toStored = (AmountStored) this.storedService.get(oqmDbIdOrName, cs, transferAmountTransaction.getToStored());
						}
					}
					default -> {
						throw new IllegalArgumentException("Cannot subtract an amount from a unique item.");
					}
				}

				fromStored.subtract(transferAmountTransaction.getAmount());
				toStored.add(transferAmountTransaction.getAmount());
				appliedTransactionBuilder.affectedStored(Set.of(fromStored.getId(), toStored.getId()));
				this.storedService.update(oqmDbIdOrName, cs, fromStored, interactingEntity,  historyDetails);
				this.storedService.update(oqmDbIdOrName, cs, toStored, interactingEntity, historyDetails);
			}
			case TRANSFER_WHOLE -> {
				TransferWholeTransaction transferWholeTransaction = (TransferWholeTransaction) itemStoredTransaction;
				ObjectId toTransferId = transferWholeTransaction.getStoredToTransfer();

				Stored toTransfer = this.storedService.get(oqmDbIdOrName, cs, toTransferId);
				if(!transferWholeTransaction.getFromBlock().equals(toTransfer.getStorageBlock())){
					throw new IllegalArgumentException("Stored to transfer not starting out in expecting block.");
				}
				toTransfer.setStorageBlock(transferWholeTransaction.getToBlock());

				appliedTransactionBuilder.affectedStored(Set.of(toTransferId));
				this.storedService.update(oqmDbIdOrName, cs, toTransfer, interactingEntity, historyDetails);
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
