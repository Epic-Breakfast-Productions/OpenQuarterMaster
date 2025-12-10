package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemAmountCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemWholeCheckout;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinFullTransaction;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.units.indriya.quantity.Quantities;

import java.util.Set;

public class CheckinFullTransactionApplier extends CheckinOutTransactionApplier<CheckinFullTransaction> {


	public CheckinFullTransactionApplier(StoredService storedService, ItemCheckoutService itemCheckoutService) {
		super(storedService, itemCheckoutService);
	}

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.CHECKIN_FULL;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		CheckinFullTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		ItemCheckout<?> checkout = this.getItemCheckoutService().get(oqmDbIdOrName, cs, transaction.getCheckoutId());

		if (!inventoryItem.getId().equals(checkout.getItem())) {
			throw new IllegalArgumentException("Checkout is not associated with the item.");
		}
		switch (checkout.getType()) {
			case AMOUNT -> {
				ItemAmountCheckout iac = (ItemAmountCheckout) checkout;

				AmountStored amountStored;
				if (transaction.getToStored() != null) {
					amountStored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getToStored());
				} else if (transaction.getToBlock() != null) {
					switch (inventoryItem.getStorageType()) {
						case BULK -> {
							try {
								amountStored = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transaction.getToBlock(), AmountStored.class);
							} catch (DbNotFoundException e) {
								amountStored = AmountStored.builder()
									.item(inventoryItem.getId())
									.storageBlock(transaction.getToBlock())
									.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
									.build();
								this.getStoredService().add(oqmDbIdOrName, cs, amountStored, interactingEntity);
							}
						}
						case AMOUNT_LIST -> {
							if (transaction.getToStored() == null) {
								amountStored = AmountStored.builder()
									.item(inventoryItem.getId())
									.storageBlock(transaction.getToBlock())
									.amount(Quantities.getQuantity(0, inventoryItem.getUnit()))
									.build();
								this.getStoredService().add(oqmDbIdOrName, cs, amountStored, interactingEntity);
							} else {
								amountStored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getToStored());
								if (!amountStored.getStorageBlock().equals(transaction.getToBlock())) {
									throw new IllegalArgumentException("To Stored given does not exist in block.");
								}
							}
						}
						default -> throw new IllegalArgumentException("Cannot add amount to unique item.");
					}
				} else {
					throw new IllegalArgumentException("Must specify a stored or block to checkin into.");
				}

				if (!inventoryItem.getId().equals(amountStored.getItem())) {
					throw new IllegalArgumentException("Stored is not associated with the item.");
				}

				amountStored.add(iac.getCheckedOut());
				this.getStoredService().update(oqmDbIdOrName, cs, amountStored, interactingEntity, historyDetails);
				affectedStored.add(amountStored);
			}
			case WHOLE -> {
				Stored checkedOut = ((ItemWholeCheckout) checkout).getCheckedOut();
				if (!inventoryItem.getId().equals(checkedOut.getItem())) {
					throw new IllegalArgumentException("Stored is not associated with the item.");
				}
				checkedOut.setStorageBlock(transaction.getToBlock());
				this.getStoredService().add(oqmDbIdOrName, cs, checkedOut, interactingEntity, historyDetails);
				affectedStored.add(checkedOut);
			}
			default -> {
				throw new IllegalArgumentException("Invalid checkout type.");
			}
		}

		checkout.setCheckInDetails(transaction.getDetails());
		checkout.setCheckInTransaction(appliedTransactionId);
		checkout.setCheckedInByEntity(interactingEntity.getId());
		this.getItemCheckoutService().update(oqmDbIdOrName, cs, checkout, interactingEntity, historyDetails);
	}
}
