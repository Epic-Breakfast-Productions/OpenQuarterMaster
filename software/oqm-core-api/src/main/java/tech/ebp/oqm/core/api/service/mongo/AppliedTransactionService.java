package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
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
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;
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
	 *
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
		HistoryDetail... details
	) throws Exception {
		try(MongoSessionWrapper csw = new MongoSessionWrapper(cs, this)) {
			return csw.runTransaction(()->{
				final ObjectId transactionId = new ObjectId();
				HistoryDetail[] historyDetails;
				{
					List<HistoryDetail> deetsCollection = new ArrayList<>();
					deetsCollection.addAll(List.of(details));
					deetsCollection.add(new ItemTransactionDetail(transactionId));
					historyDetails = deetsCollection.toArray(new HistoryDetail[0]);
				}
				AppliedTransaction.Builder<?, ?> appliedTransactionBuilder = AppliedTransaction.builder()
					.id(transactionId)
					.inventoryItem(inventoryItem.getId())
					.transaction(itemStoredTransaction)
					.entity(interactingEntity.getId());

				switch (itemStoredTransaction.getTransactionType()) {
					case ADD_AMOUNT -> {
						AddAmountTransaction addAmountTransaction = (AddAmountTransaction) itemStoredTransaction;
						AmountStored stored;
						switch (inventoryItem.getStorageType()) {
							case BULK -> {
								if (addAmountTransaction.getToBlock() != null) {
									try {
										stored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), addAmountTransaction.getToBlock(), AmountStored.class);
									} catch (DbNotFoundException e) {
										stored = AmountStored.builder()
											.item(inventoryItem.getId())
											.storageBlock(addAmountTransaction.getToBlock())
											.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
											.build();
										this.storedService.add(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity);
									}
									if (addAmountTransaction.getToStored() != null){
										if(!stored.getId().equals(addAmountTransaction.getToStored())){
											throw new IllegalArgumentException("To Stored given does not match stored found in block.");
										}
									}
								} else if (addAmountTransaction.getToStored() != null) {
									stored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), addAmountTransaction.getToStored());
								} else {
									throw new IllegalArgumentException("No to block or stored given.");
								}
							}
							case AMOUNT_LIST -> {
								if (addAmountTransaction.getToStored() == null) {
									stored = AmountStored.builder()
										.item(inventoryItem.getId())
										.storageBlock(addAmountTransaction.getToBlock())
										.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
										.build();
									this.storedService.add(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity);
								} else {
									stored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), addAmountTransaction.getToStored());
									if(!stored.getStorageBlock().equals(addAmountTransaction.getToBlock())){
										throw new IllegalArgumentException("To Stored given does not exist in block.");
									}
								}
							}
							default -> {
								throw new IllegalArgumentException("Cannot add an amount to a unique item.");
							}
						}
						appliedTransactionBuilder.affectedStored(Set.of(stored.getId()));
						stored.add(addAmountTransaction.getAmount());

						appliedTransactionBuilder.affectedStored(Set.of(stored.getId()));
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity, historyDetails);
					}
					case ADD_WHOLE -> {
						AddWholeTransaction awt = (AddWholeTransaction) itemStoredTransaction;
						Stored stored = awt.getToAdd();

						if(!inventoryItem.getId().equals(stored.getItem())){
							throw new IllegalArgumentException("Stored given is not associated with item.");
						}

						if(inventoryItem.getStorageType() == StorageType.BULK){
							throw new IllegalArgumentException("Cannot add whole item to a bulk storage typed item.");
						}

						if(!awt.getToBlock().equals(stored.getStorageBlock())){
							throw new IllegalArgumentException("To Block given does not match block marked in stored.");
						}

						this.storedService.add(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity, historyDetails);
						appliedTransactionBuilder.affectedStored(Set.of(stored.getId()));
					}
					case CHECKIN_FULL -> {
						CheckinFullTransaction cfTransaction = (CheckinFullTransaction) itemStoredTransaction;
						ItemCheckout<?> checkout = this.itemCheckoutService.get(oqmDbIdOrName, csw.getClientSession(), cfTransaction.getCheckoutId());

						switch (checkout.getCheckoutType()) {
							case AMOUNT -> {
								ItemAmountCheckout iac = (ItemAmountCheckout) checkout;

								AmountStored amountStored;
								if (cfTransaction.getToStored() != null) {
									amountStored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), cfTransaction.getToStored());
									//TODO:: create if not present?
								} else if (cfTransaction.getToBlock() != null) {
									switch (inventoryItem.getStorageType()) {
										case BULK -> {
											amountStored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), cfTransaction.getToBlock(), AmountStored.class);
											//TODO:: create if not present?
										}
										default ->
											throw new IllegalArgumentException("Must specify a stored to checkin into for item list.");
									}
								} else {
									throw new IllegalArgumentException("Must specify a stored or block to checkin into.");
								}

								amountStored.add(iac.getCheckedOut());
								this.storedService.update(oqmDbIdOrName, csw.getClientSession(), amountStored, interactingEntity, historyDetails);
								appliedTransactionBuilder.affectedStored(Set.of(amountStored.getId()));
							}
							case WHOLE -> {
								Stored checkedOut = ((ItemWholeCheckout) checkout).getCheckedOut();
								checkedOut.setStorageBlock(cfTransaction.getToBlock());
								this.storedService.add(oqmDbIdOrName, csw.getClientSession(), checkedOut, interactingEntity, historyDetails);
								appliedTransactionBuilder.affectedStored(Set.of(checkedOut.getId()));
							}
						}
						checkout.setCheckInDetails(cfTransaction.getDetails());
						checkout.setCheckInTransaction(transactionId);
						this.itemCheckoutService.update(oqmDbIdOrName, csw.getClientSession(), checkout, interactingEntity, historyDetails);
					}
					case CHECKIN_PART -> {
						CheckinPartTransaction checkinPartTransaction = (CheckinPartTransaction) itemStoredTransaction;
						//TODO
						throw new NotImplementedException("Checking in only a part of a checked out item not supported yet.");
					}
					case CHECKOUT_AMOUNT -> {
						CheckoutAmountTransaction checkoutAmountTransaction = (CheckoutAmountTransaction) itemStoredTransaction;
						AmountStored stored;

						switch (inventoryItem.getStorageType()) {
							case BULK -> {
								if (checkoutAmountTransaction.getFromBlock() != null) {
									stored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), checkoutAmountTransaction.getFromBlock(), AmountStored.class);
								} else if (checkoutAmountTransaction.getFromStored() != null) {
									stored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), checkoutAmountTransaction.getFromStored());
								} else {
									throw new IllegalArgumentException("No stored or block given to checkout from.");
								}
							}
							case AMOUNT_LIST -> {
								if (checkoutAmountTransaction.getFromStored() == null) {
									throw new IllegalArgumentException("No stored given to checkout from.");
								}
								stored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), checkoutAmountTransaction.getFromStored());
							}
							default -> throw new IllegalArgumentException("Cannot checkout an amount from a unique type.");
						}

						appliedTransactionBuilder.affectedStored(Set.of(stored.getId()));
						ItemCheckout.Builder<?, ?, ?> checkoutBuilder = ItemAmountCheckout.builder()
							.item(inventoryItem.getId())
							.checkoutDetails(checkoutAmountTransaction.getCheckoutDetails())
							.fromStoredId(stored.getId())
							.checkedOutFromBlock(stored.getStorageBlock())
							.checkedOut(checkoutAmountTransaction.getAmount())
							.checkOutTransaction(transactionId);

						stored.subtract(checkoutAmountTransaction.getAmount());
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity, historyDetails);
						this.itemCheckoutService.add(oqmDbIdOrName, csw.getClientSession(), checkoutBuilder.build(), interactingEntity);
					}
					case CHECKOUT_WHOLE -> {
						CheckoutWholeTransaction cwTransaction = (CheckoutWholeTransaction) itemStoredTransaction;
						Stored affectedStored = this.storedService.get(oqmDbIdOrName, cwTransaction.getToCheckout());
						appliedTransactionBuilder.affectedStored(Set.of(affectedStored.getId()));
						ItemCheckout.Builder<?, ?, ?> checkoutBuilder;
						switch (inventoryItem.getStorageType()) {
							case BULK -> {
								AmountStored affectedAmountStored = (AmountStored) affectedStored;
								checkoutBuilder = ItemAmountCheckout.builder()
									.checkedOut(affectedAmountStored.getAmount());
								affectedAmountStored.setAmount(Quantities.getQuantity(0, affectedAmountStored.getAmount().getUnit()));
								this.storedService.update(oqmDbIdOrName, csw.getClientSession(), affectedAmountStored, interactingEntity, historyDetails);
							}
							case AMOUNT_LIST, UNIQUE_MULTI, UNIQUE_SINGLE -> {
								checkoutBuilder = ItemWholeCheckout.builder()
									.checkedOut(affectedStored);
								this.storedService.remove(oqmDbIdOrName, csw.getClientSession(), affectedStored.getId(), interactingEntity, historyDetails);
							}
							default -> {
								throw new IllegalStateException("Storage type not supported. This should never happen.");
							}
						}
						checkoutBuilder.item(inventoryItem.getId())
							.checkedOutFromBlock(affectedStored.getStorageBlock())
							.checkoutDetails(cwTransaction.getCheckoutDetails())
							.checkOutTransaction(transactionId)
						;
						this.itemCheckoutService.add(oqmDbIdOrName, csw.getClientSession(), checkoutBuilder.build(), interactingEntity);
					}
					case SUBTRACT_AMOUNT -> {
						SubAmountTransaction subAmountTransaction = (SubAmountTransaction) itemStoredTransaction;
						AmountStored stored;
						switch (inventoryItem.getStorageType()) {
							case BULK -> {
								stored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), subAmountTransaction.getFromBlock(), AmountStored.class);
							}
							case AMOUNT_LIST -> {
								stored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), subAmountTransaction.getFromStored());
							}
							default -> {
								throw new IllegalArgumentException("Cannot subtract an amount from a unique item.");
							}
						}

						if(subAmountTransaction.getFromStored() != null && !stored.getId().equals(subAmountTransaction.getFromStored())){
							throw new IllegalArgumentException("Stored id in transaction not the id of stored found.");
						}
						if(!stored.getStorageBlock().equals(subAmountTransaction.getFromBlock())){
							throw new IllegalArgumentException("Stored retrieved not in specified block.");
						}
						stored.subtract(subAmountTransaction.getAmount());

						appliedTransactionBuilder.affectedStored(Set.of(stored.getId()));
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity, historyDetails);
					}
					case SUBTRACT_WHOLE -> {
						SubWholeTransaction subWholeTransaction = (SubWholeTransaction) itemStoredTransaction;
						ObjectId toSubtract = subWholeTransaction.getToSubtract();

						appliedTransactionBuilder.affectedStored(Set.of(toSubtract));
						this.storedService.remove(oqmDbIdOrName, csw.getClientSession(), toSubtract, interactingEntity, historyDetails);
					}
					case TRANSFER_AMOUNT -> {
						TransferAmountTransaction transferAmountTransaction = (TransferAmountTransaction) itemStoredTransaction;
						AmountStored fromStored;
						AmountStored toStored;

						switch (inventoryItem.getStorageType()) {
							case BULK -> {
								fromStored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), transferAmountTransaction.getFromBlock(), AmountStored.class);
								try {
									toStored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), transferAmountTransaction.getToBlock(), AmountStored.class);
								} catch (DbNotFoundException e) {
									toStored = AmountStored.builder()
										.item(inventoryItem.getId())
										.storageBlock(transferAmountTransaction.getToBlock())
										.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
										.build();
									this.storedService.add(oqmDbIdOrName, csw.getClientSession(), toStored, interactingEntity);
								}
							}
							case AMOUNT_LIST -> {
								fromStored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), transferAmountTransaction.getFromStored());
								if (transferAmountTransaction.getToStored() == null) {
									toStored = AmountStored.builder()
										.item(inventoryItem.getId())
										.storageBlock(transferAmountTransaction.getToBlock())
										.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
										.build();
									this.storedService.add(oqmDbIdOrName, csw.getClientSession(), toStored, interactingEntity);
								} else {
									toStored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), transferAmountTransaction.getToStored());
								}
							}
							default -> {
								throw new IllegalArgumentException("Cannot subtract an amount from a unique item.");
							}
						}

						fromStored.subtract(transferAmountTransaction.getAmount());
						toStored.add(transferAmountTransaction.getAmount());
						appliedTransactionBuilder.affectedStored(Set.of(fromStored.getId(), toStored.getId()));
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), fromStored, interactingEntity, historyDetails);
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), toStored, interactingEntity, historyDetails);
					}
					case TRANSFER_WHOLE -> {
						TransferWholeTransaction transferWholeTransaction = (TransferWholeTransaction) itemStoredTransaction;
						ObjectId toTransferId = transferWholeTransaction.getStoredToTransfer();

						Stored toTransfer = this.storedService.get(oqmDbIdOrName, csw.getClientSession(), toTransferId);
						if (!transferWholeTransaction.getFromBlock().equals(toTransfer.getStorageBlock())) {
							throw new IllegalArgumentException("Stored to transfer not starting out in expecting block.");
						}
						toTransfer.setStorageBlock(transferWholeTransaction.getToBlock());

						appliedTransactionBuilder.affectedStored(Set.of(toTransferId));
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), toTransfer, interactingEntity, historyDetails);
					}
				}

				appliedTransactionBuilder.statsAfterApply(this.storedService.getItemStats(oqmDbIdOrName, csw.getClientSession(), inventoryItem));

				ObjectId newId = this.add(oqmDbIdOrName, appliedTransactionBuilder.build());

				return newId;
			});
		} catch (Exception e) {
			log.error("Failed to apply transaction: ", e);
			throw e;
		}
	}

	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return null;
	}
}
