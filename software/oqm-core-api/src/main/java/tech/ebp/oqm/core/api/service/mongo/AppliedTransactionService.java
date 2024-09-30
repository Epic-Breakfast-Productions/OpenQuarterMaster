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
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.ReturnFullCheckinDetails;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinFullTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinLossTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinPartTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set.SetAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferWholeTransaction;
import tech.ebp.oqm.core.api.model.rest.search.AppliedTransactionSearch;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;
import tech.units.indriya.quantity.Quantities;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
	 * <p>
	 * TODO:: do... something... with this madness
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
		try (MongoSessionWrapper csw = new MongoSessionWrapper(cs, this)) {
			return csw.runTransaction(() -> {
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
									if (addAmountTransaction.getToStored() != null) {
										if (!stored.getId().equals(addAmountTransaction.getToStored())) {
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
									if (!stored.getStorageBlock().equals(addAmountTransaction.getToBlock())) {
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

						appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(stored.getId())));
						stored.add(addAmountTransaction.getAmount());

						appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(stored.getId())));
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity, historyDetails);
					}
					case ADD_WHOLE -> {
						AddWholeTransaction awt = (AddWholeTransaction) itemStoredTransaction;
						Stored stored = awt.getToAdd();

						if (!inventoryItem.getId().equals(stored.getItem())) {
							throw new IllegalArgumentException("Stored given is not associated with item.");
						}

						if (inventoryItem.getStorageType() == StorageType.BULK) {
							throw new IllegalArgumentException("Cannot add whole item to a bulk storage typed item.");
						}

						if (!awt.getToBlock().equals(stored.getStorageBlock())) {
							throw new IllegalArgumentException("To Block given does not match block marked in stored.");
						}

						this.storedService.add(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity, historyDetails);
						appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(stored.getId())));
					}
					case CHECKIN_FULL -> {
						CheckinFullTransaction cfTransaction = (CheckinFullTransaction) itemStoredTransaction;
						ItemCheckout<?> checkout = this.itemCheckoutService.get(oqmDbIdOrName, csw.getClientSession(), cfTransaction.getCheckoutId());

						if (!inventoryItem.getId().equals(checkout.getItem())) {
							throw new IllegalArgumentException("Checkout is not associated with the item.");
						}
						switch (checkout.getCheckoutType()) {
							case AMOUNT -> {
								ItemAmountCheckout iac = (ItemAmountCheckout) checkout;

								AmountStored amountStored;
								if (cfTransaction.getToStored() != null) {
									try {
										amountStored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), cfTransaction.getToBlock(), AmountStored.class);
									} catch (DbNotFoundException e) {
										amountStored = AmountStored.builder()
											.item(inventoryItem.getId())
											.storageBlock(cfTransaction.getToBlock())
											.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
											.build();
										this.storedService.add(oqmDbIdOrName, csw.getClientSession(), amountStored, interactingEntity);
									}
								} else if (cfTransaction.getToBlock() != null) {
									switch (inventoryItem.getStorageType()) {
										case BULK -> {
											try {
												amountStored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), cfTransaction.getToBlock(), AmountStored.class);
											} catch (DbNotFoundException e) {
												amountStored = AmountStored.builder()
													.item(inventoryItem.getId())
													.storageBlock(cfTransaction.getToBlock())
													.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
													.build();
												this.storedService.add(oqmDbIdOrName, csw.getClientSession(), amountStored, interactingEntity);
											}
										}
										case AMOUNT_LIST -> {
											if (cfTransaction.getToStored() == null) {
												amountStored = AmountStored.builder()
													.item(inventoryItem.getId())
													.storageBlock(cfTransaction.getToBlock())
													.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
													.build();
												this.storedService.add(oqmDbIdOrName, csw.getClientSession(), amountStored, interactingEntity);
											} else {
												amountStored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), cfTransaction.getToStored());
												if (!amountStored.getStorageBlock().equals(cfTransaction.getToBlock())) {
													throw new IllegalArgumentException("To Stored given does not exist in block.");
												}
											}
										}
										default ->
											throw new IllegalArgumentException("Cannot add amount to unique item.");
									}
								} else {
									throw new IllegalArgumentException("Must specify a stored or block to checkin into.");
								}

								if (!inventoryItem.getId().equals(amountStored.getItem())) {
									throw new IllegalArgumentException("Stored is not associated with the item.");
								}

								amountStored.add(iac.getCheckedOut());
								this.storedService.update(oqmDbIdOrName, csw.getClientSession(), amountStored, interactingEntity, historyDetails);
								appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(amountStored.getId())));
							}
							case WHOLE -> {
								Stored checkedOut = ((ItemWholeCheckout) checkout).getCheckedOut();
								if (!inventoryItem.getId().equals(checkedOut.getItem())) {
									throw new IllegalArgumentException("Stored is not associated with the item.");
								}
								checkedOut.setStorageBlock(cfTransaction.getToBlock());
								this.storedService.add(oqmDbIdOrName, csw.getClientSession(), checkedOut, interactingEntity, historyDetails);
								appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(checkedOut.getId())));
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
					case CHECKIN_LOSS -> {
						CheckinLossTransaction clt = (CheckinLossTransaction) itemStoredTransaction;
						//TODO
						throw new NotImplementedException("This should be implemented.");
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
							default ->
								throw new IllegalArgumentException("Cannot checkout an amount from a unique type.");
						}

						if (!inventoryItem.getId().equals(stored.getItem())) {
							throw new IllegalArgumentException("Stored is not associated with the item.");
						}
						if (checkoutAmountTransaction.getFromBlock() != null && !stored.getStorageBlock().equals(checkoutAmountTransaction.getFromBlock())) {
							throw new IllegalArgumentException("From Storage block given mismatched stored's block.");
						}

						appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(stored.getId())));
						ItemCheckout.Builder<?, ?, ?> checkoutBuilder = ItemAmountCheckout.builder()
							.item(inventoryItem.getId())
							.checkoutDetails(checkoutAmountTransaction.getCheckoutDetails())
							.fromStoredId(stored.getId())
							.checkedOut(checkoutAmountTransaction.getAmount())
							.checkOutTransaction(transactionId);

						stored.subtract(checkoutAmountTransaction.getAmount());
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity, historyDetails);
						this.itemCheckoutService.add(oqmDbIdOrName, csw.getClientSession(), checkoutBuilder.build(), interactingEntity);
					}
					case CHECKOUT_WHOLE -> {
						CheckoutWholeTransaction cwTransaction = (CheckoutWholeTransaction) itemStoredTransaction;
						Stored affectedStored = this.storedService.get(oqmDbIdOrName, cwTransaction.getToCheckout());

						if (!inventoryItem.getId().equals(affectedStored.getItem())) {
							throw new IllegalArgumentException("Stored is not associated with the item.");
						}

						appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(affectedStored.getId())));
						ItemCheckout.Builder<?, ?, ?> checkoutBuilder = ItemWholeCheckout.builder()
							.checkoutDetails(cwTransaction.getCheckoutDetails())
							.checkOutTransaction(transactionId)
							.item(inventoryItem.getId())
							.checkedOut(affectedStored);

						this.storedService.remove(oqmDbIdOrName, csw.getClientSession(), affectedStored.getId(), interactingEntity, historyDetails);
						this.itemCheckoutService.add(oqmDbIdOrName, csw.getClientSession(), checkoutBuilder.build(), interactingEntity);
					}
					case SET_AMOUNT -> {
						SetAmountTransaction sat = (SetAmountTransaction) itemStoredTransaction;
						AmountStored stored;
						switch (inventoryItem.getStorageType()) {
							case BULK -> {
								if (sat.getBlock() != null) {
									try {
										stored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), sat.getBlock(), AmountStored.class);
									} catch (DbNotFoundException e) {
										stored = AmountStored.builder()
											.item(inventoryItem.getId())
											.storageBlock(sat.getBlock())
											.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
											.build();
										this.storedService.add(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity);
									}
									if (sat.getStored() != null) {
										if (!stored.getId().equals(sat.getStored())) {
											throw new IllegalArgumentException("To Stored given does not match stored found in block.");
										}
									}
								} else if (sat.getStored() != null) {
									stored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), sat.getStored());
								} else {
									throw new IllegalArgumentException("No to block or stored given.");
								}
							}
							case AMOUNT_LIST -> {
								if (sat.getStored() == null) {
									throw new IllegalArgumentException("Must specify a stored to set the amount of.");
								} else {
									stored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), sat.getStored());
									if (!stored.getStorageBlock().equals(sat.getBlock())) {
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

						appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(stored.getId())));
						stored.setAmount(sat.getAmount());

						appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(stored.getId())));
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity, historyDetails);
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

						if (!inventoryItem.getId().equals(stored.getItem())) {
							throw new IllegalArgumentException("Stored is not associated with the item.");
						}

						if (subAmountTransaction.getFromStored() != null && !stored.getId().equals(subAmountTransaction.getFromStored())) {
							throw new IllegalArgumentException("Stored id in transaction not the id of stored found.");
						}
						if (!stored.getStorageBlock().equals(subAmountTransaction.getFromBlock())) {
							throw new IllegalArgumentException("Stored retrieved not in specified block.");
						}
						stored.subtract(subAmountTransaction.getAmount());

						appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(stored.getId())));
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), stored, interactingEntity, historyDetails);
					}
					case SUBTRACT_WHOLE -> {
						SubWholeTransaction subWholeTransaction = (SubWholeTransaction) itemStoredTransaction;
						Stored toSubtract = this.storedService.get(oqmDbIdOrName, cs, subWholeTransaction.getToSubtract());

						if (!inventoryItem.getId().equals(toSubtract.getItem())) {
							throw new IllegalArgumentException("Stored is not associated with the item.");
						}

						appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(toSubtract.getId())));
						this.storedService.remove(oqmDbIdOrName, csw.getClientSession(), toSubtract.getId(), interactingEntity, historyDetails);
					}
					case TRANSFER_AMOUNT -> {
						TransferAmountTransaction tat = (TransferAmountTransaction) itemStoredTransaction;
						AmountStored fromStored;
						AmountStored toStored;

						switch (inventoryItem.getStorageType()) {
							case BULK -> {
								fromStored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), tat.getFromBlock(), AmountStored.class);
								try {
									toStored = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId(), tat.getToBlock(), AmountStored.class);
								} catch (DbNotFoundException e) {
									toStored = AmountStored.builder()
										.item(inventoryItem.getId())
										.storageBlock(tat.getToBlock())
										.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
										.build();
									this.storedService.add(oqmDbIdOrName, csw.getClientSession(), toStored, interactingEntity);
								}
							}
							case AMOUNT_LIST -> {
								fromStored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), tat.getFromStored());
								if (tat.getToStored() == null) {
									toStored = AmountStored.builder()
										.item(inventoryItem.getId())
										.storageBlock(tat.getToBlock())
										.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
										.build();
									this.storedService.add(oqmDbIdOrName, csw.getClientSession(), toStored, interactingEntity);
								} else {
									toStored = (AmountStored) this.storedService.get(oqmDbIdOrName, csw.getClientSession(), tat.getToStored());
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

						if (tat.getFromStored() != null && !tat.getFromStored().equals(fromStored.getId())) {
							throw new IllegalArgumentException("From Stored retrieved not in specified block.");
						}
						if (tat.getToStored() != null && !tat.getToStored().equals(toStored.getId())) {
							throw new IllegalArgumentException("To Stored retrieved not in specified block.");
						}

						fromStored.subtract(tat.getAmount());
						toStored.add(tat.getAmount());
						LinkedHashSet<ObjectId> affectedStored = new LinkedHashSet<>();
						affectedStored.add(toStored.getId());
						affectedStored.add(fromStored.getId());
						appliedTransactionBuilder.affectedStored(affectedStored);
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), fromStored, interactingEntity, historyDetails);
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), toStored, interactingEntity, historyDetails);
					}
					case TRANSFER_WHOLE -> {
						TransferWholeTransaction twt = (TransferWholeTransaction) itemStoredTransaction;

						Stored toTransfer;
						switch (inventoryItem.getStorageType()) {
							case BULK, UNIQUE_SINGLE -> {
								toTransfer = this.storedService.getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), twt.getFromBlock(), Stored.class);

								if (twt.getStoredToTransfer() != null && !twt.getStoredToTransfer().equals(toTransfer.getId())) {
									throw new IllegalArgumentException("Stored id given mismatched id from gotten stored.");
								}
							}
							case AMOUNT_LIST, UNIQUE_MULTI -> {
								toTransfer = this.storedService.get(oqmDbIdOrName, csw.getClientSession(), twt.getStoredToTransfer());

								if (!twt.getFromBlock().equals(toTransfer.getStorageBlock())) {
									throw new IllegalArgumentException("Stored found not in specified block.");
								}
							}
							default ->
								throw new IllegalArgumentException("Unsupported storage type. This should never happen.");
						}

						if (!inventoryItem.getId().equals(toTransfer.getItem())) {
							throw new IllegalArgumentException("Stored is not associated with the item.");
						}

						toTransfer.setStorageBlock(twt.getToBlock());

						appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(Set.of(toTransfer.getId())));
						this.storedService.update(oqmDbIdOrName, csw.getClientSession(), toTransfer, interactingEntity, historyDetails);
					}
				}

				//TODO:: process expired, low stock




				appliedTransactionBuilder.statsAfterApply(this.storedService.getItemStats(oqmDbIdOrName, csw.getClientSession(), inventoryItem.getId()));

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
