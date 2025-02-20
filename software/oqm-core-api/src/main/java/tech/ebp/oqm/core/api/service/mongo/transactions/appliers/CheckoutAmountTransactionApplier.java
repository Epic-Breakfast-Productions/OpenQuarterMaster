package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import org.apache.commons.lang3.NotImplementedException;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemAmountCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinPartTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;

import java.util.LinkedHashSet;
import java.util.Set;

public class CheckoutAmountTransactionApplier extends CheckinOutTransactionApplier<CheckoutAmountTransaction> {

	public CheckoutAmountTransactionApplier(StoredService storedService, ItemCheckoutService itemCheckoutService) {
		super(storedService, itemCheckoutService);
	}

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.CHECKOUT_AMOUNT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		CheckoutAmountTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		AmountStored stored;

		switch (inventoryItem.getStorageType()) {
			case BULK -> {
				if (transaction.getFromBlock() != null) {
					stored = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName,cs, inventoryItem.getId(), transaction.getFromBlock(), AmountStored.class);
				} else if (transaction.getFromStored() != null) {
					stored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getFromStored());
				} else {
					throw new IllegalArgumentException("No stored or block given to checkout from.");
				}
			}
			case AMOUNT_LIST -> {
				if (transaction.getFromStored() == null) {
					throw new IllegalArgumentException("No stored given to checkout from.");
				}
				stored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getFromStored());
			}
			default ->
				throw new IllegalArgumentException("Cannot checkout an amount from a unique type.");
		}

		if (!inventoryItem.getId().equals(stored.getItem())) {
			throw new IllegalArgumentException("Stored is not associated with the item.");
		}
		if (transaction.getFromBlock() != null && !stored.getStorageBlock().equals(transaction.getFromBlock())) {
			throw new IllegalArgumentException("From Storage block given mismatched stored's block.");
		}

		ItemCheckout.Builder<?, ?, ?> checkoutBuilder = ItemAmountCheckout.builder()
			.item(inventoryItem.getId())
			.checkoutDetails(transaction.getCheckoutDetails())
			.fromStoredId(stored.getId())
			.checkedOut(transaction.getAmount())
			.checkOutTransaction(appliedTransactionId);

		stored.subtract(transaction.getAmount());
		affectedStored.add(stored);
		this.getStoredService().update(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
		this.getItemCheckoutService().add(oqmDbIdOrName, cs, checkoutBuilder.build(), interactingEntity);
	}
}
