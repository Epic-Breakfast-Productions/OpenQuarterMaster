package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import org.apache.commons.lang3.NotImplementedException;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinPartTransaction;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;

import java.util.Set;

public class CheckinPartTransactionApplier extends CheckinOutTransactionApplier<CheckinPartTransaction> {


	public CheckinPartTransactionApplier(StoredService storedService, ItemCheckoutService itemCheckoutService) {
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
		CheckinPartTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		//TODO
		throw new NotImplementedException("Checking in only a part of a checked out item not supported yet.");
	}
}
