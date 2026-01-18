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
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutWholeTransaction;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;

import java.util.LinkedHashSet;
import java.util.Set;

public class CheckoutWholeTransactionApplier extends CheckinOutTransactionApplier<CheckoutWholeTransaction> {
	
	public CheckoutWholeTransactionApplier(StoredService storedService, ItemCheckoutService itemCheckoutService) {
		super(storedService, itemCheckoutService);
	}
	
	@Override
	public TransactionType getTransactionType() {
		return TransactionType.CHECKOUT_WHOLE;
	}
	
	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		CheckoutWholeTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		Stored storedCheckingOut = this.getStoredService().get(oqmDbIdOrName, transaction.getToCheckout());
		
		if (!inventoryItem.getId().equals(storedCheckingOut.getItem())) {
			throw new IllegalArgumentException("Stored is not associated with the item.");
		}
		
		ItemCheckout.ItemCheckoutBuilder<?, ?, ?> checkoutBuilder = ItemWholeCheckout.builder()
															.checkedOutByEntity(interactingEntity.getId())
															.checkoutDetails(transaction.getCheckoutDetails())
															.checkOutTransaction(appliedTransactionId)
															.item(inventoryItem.getId())
															.checkedOut(storedCheckingOut);
		
		this.getStoredService().remove(oqmDbIdOrName, cs, storedCheckingOut.getId(), interactingEntity, historyDetails);
		this.getItemCheckoutService().add(oqmDbIdOrName, cs, checkoutBuilder.build(), interactingEntity);
		affectedStored.add(storedCheckingOut);
	}
}
