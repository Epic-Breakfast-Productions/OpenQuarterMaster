package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.service.mongo.StoredService;

import java.util.Set;

/**
 * Abstract applier of transactions.
 * @param <T> The type of transaction being applied.
 */
@AllArgsConstructor
public abstract class TransactionApplier<T extends ItemStoredTransaction> {

	@Getter
	private StoredService storedService;

	public abstract TransactionType getTransactionType();

	public abstract void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		T transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	);
}
