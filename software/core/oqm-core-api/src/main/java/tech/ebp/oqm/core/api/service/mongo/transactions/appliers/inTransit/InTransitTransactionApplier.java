package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit;

import com.mongodb.client.ClientSession;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.InTransitLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StorageBlockLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StoredLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.TransactionApplier;

public abstract class InTransitTransactionApplier<T extends ItemStoredTransaction> extends TransactionApplier<T> {




	protected void assertInTransitValid(
		ClientSession cs,
		String oqmDbNameOrId,
		InventoryItem item,
		InTransit inTransit
	){
		//TODO:: think of something not covered in stored service ensure valid
	}

}
