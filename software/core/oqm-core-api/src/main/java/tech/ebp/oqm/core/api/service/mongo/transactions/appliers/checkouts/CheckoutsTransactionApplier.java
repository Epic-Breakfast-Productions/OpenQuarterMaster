package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.checkouts;

import com.mongodb.client.ClientSession;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.CheckoutsTransaction;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.TransactionApplier;

public abstract class CheckoutsTransactionApplier<T extends CheckoutsTransaction> extends TransactionApplier<T> {

}
