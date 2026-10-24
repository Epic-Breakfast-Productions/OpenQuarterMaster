package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.checkouts.checkin;

import jakarta.inject.Inject;
import lombok.Getter;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.TransactionApplier;

/**
 * Abstract applier of transactions.
 * @param <T> The type of transaction being applied.
 */
public abstract class CheckinOutTransactionApplier<T extends ItemStoredTransaction> extends TransactionApplier<T> {

	@Inject
	@Getter
	ItemCheckoutService itemCheckoutService;
}
