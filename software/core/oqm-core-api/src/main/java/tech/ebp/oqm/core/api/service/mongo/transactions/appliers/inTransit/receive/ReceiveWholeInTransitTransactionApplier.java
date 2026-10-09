package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.receive;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.InTransitLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.LocaleType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StorageBlockLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveWholeInTransitTransaction;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitTransactionApplier;

import java.util.Set;

/**
 * Applier to handle AddAmountTransactions.
 *
 */
@ApplicationScoped
public class ReceiveWholeInTransitTransactionApplier extends InTransitTransactionApplier<ReceiveWholeInTransitTransaction> {

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.RECEIVE_WHOLE_IN_TRANSIT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		ReceiveWholeInTransitTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		Stored inTransit = this.getStoredService().get(
			oqmDbIdOrName,
			cs,
			transaction.getInTransitStored()
		);

		if (!inventoryItem.getId().equals(inTransit.getItem())) {
			throw new IllegalArgumentException("Stored must be of item type");
		}

		if (inTransit.getState().getType() != StoredStateType.IN_TRANSIT) {
			throw new IllegalArgumentException("Stored must be in transit in order to receive.");
		}

		{
			ObjectId toBlock = transaction.getToBlock();

			if (toBlock == null) {
				InTransitLocale to = ((InTransit) inTransit.getState()).getTo();

				if (to.getType() != LocaleType.STORAGE_BLOCK) {
					throw new IllegalArgumentException("Cannot use 'to' in in-transit details, not specified to go to storage block");
				}

				toBlock = ((StorageBlockLocale) to).getStorageBlock();
			}

			inTransit.setState(
				StoredInBlock.builder()
					.storageBlock(toBlock)
					.build()
			);
		}

		affectedStored.add(inTransit);
		this.getStoredService().update(oqmDbIdOrName, cs, inTransit, interactingEntity, historyDetails);
	}
}
