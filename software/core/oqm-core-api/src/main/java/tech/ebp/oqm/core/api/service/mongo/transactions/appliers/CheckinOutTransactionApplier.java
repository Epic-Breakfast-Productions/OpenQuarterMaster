package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;

import java.util.Set;

/**
 * Abstract applier of transactions.
 * @param <T> The type of transaction being applied.
 */
public abstract class CheckinOutTransactionApplier<T extends ItemStoredTransaction> extends TransactionApplier<T> {

	@Getter
	private ItemCheckoutService itemCheckoutService;

	public CheckinOutTransactionApplier(StoredService storedService, ItemCheckoutService itemCheckoutService) {
		super(storedService);
		this.itemCheckoutService = itemCheckoutService;
	}
}
