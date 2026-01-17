package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import lombok.extern.slf4j.Slf4j;
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

import javax.measure.Quantity;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
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
					stored = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transaction.getFromBlock(), AmountStored.class);
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
			default -> throw new IllegalArgumentException("Cannot checkout an amount from a unique type.");
		}
		
		if (!inventoryItem.getId().equals(stored.getItem())) {
			throw new IllegalArgumentException("Stored is not associated with the item.");
		}
		if (transaction.getFromBlock() != null && !stored.getStorageBlock().equals(transaction.getFromBlock())) {
			throw new IllegalArgumentException("From Storage block given mismatched stored's block.");
		}
		
		if (transaction.getAmount() == null && !transaction.isAll()) {
			throw new IllegalArgumentException("Must specify amount to checkout or specify checking out all in stored.");
		}
		if (transaction.getAmount() != null && transaction.isAll()) {
			throw new IllegalArgumentException("Must specify only one of amount to checkout or checking out all in stored.");
		}
		
		Quantity amount = null;
		
		if (transaction.getAmount() != null) {
			amount = transaction.getAmount();
		}
		if (transaction.isAll()) {
			amount = stored.getAmount();
		}

		if (amount.getValue().equals(0)) {
			throw new IllegalArgumentException("Amount to checkout must be greater than zero.");
		}

		ItemCheckout.Builder<?, ?, ?> checkoutBuilder = ItemAmountCheckout.builder()
															.checkedOutByEntity(interactingEntity.getId())
															.item(inventoryItem.getId())
															.checkoutDetails(transaction.getCheckoutDetails())
															.fromStored(stored.getId())
															.fromBlock(stored.getStorageBlock())
															.checkedOut(amount)
															.checkOutTransaction(appliedTransactionId);
		
		stored.subtract(amount);
		affectedStored.add(stored);
		this.getStoredService().update(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
		this.getItemCheckoutService().add(oqmDbIdOrName, cs, checkoutBuilder.build(), interactingEntity);
	}
}
