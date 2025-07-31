package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubWholeTransaction;
import tech.ebp.oqm.core.api.service.mongo.StoredService;

import java.util.LinkedHashSet;
import java.util.Set;

public class SubtractWholeTransactionApplier extends TransactionApplier<SubWholeTransaction> {

	public SubtractWholeTransactionApplier(StoredService storedService) {
		super(storedService);
	}

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.SUBTRACT_WHOLE;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		SubWholeTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		Stored toSubtract = this.getStoredService().get(oqmDbIdOrName, cs, transaction.getToSubtract());

		if (!inventoryItem.getId().equals(toSubtract.getItem())) {
			throw new IllegalArgumentException("Stored is not associated with the item.");
		}

		affectedStored.add(toSubtract);
		this.getStoredService().remove(oqmDbIdOrName, cs, toSubtract.getId(), interactingEntity, historyDetails);
	}
}
