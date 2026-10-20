package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.checkouts;

import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.CheckoutsTransaction;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.TransactionApplier;

public abstract class CheckoutsTransactionApplier<T extends CheckoutsTransaction> extends TransactionApplier<T> {

}
