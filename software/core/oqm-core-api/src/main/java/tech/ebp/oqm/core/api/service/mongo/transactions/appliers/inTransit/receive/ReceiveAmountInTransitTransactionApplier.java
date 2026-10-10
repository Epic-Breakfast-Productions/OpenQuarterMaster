package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.receive;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.InTransitLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.LocaleType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StorageBlockLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveAmountInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitTransactionApplier;

import javax.measure.Quantity;
import java.util.Set;

/**
 * Applier to handle AddAmountTransactions.
 *
 */
@ApplicationScoped
public class ReceiveAmountInTransitTransactionApplier extends InTransitTransactionApplier<ReceiveAmountInTransitTransaction> {

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.RECEIVE_AMOUNT_IN_TRANSIT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		ReceiveAmountInTransitTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		Stored inTransit = this.getAndAssertInTransitStored(oqmDbIdOrName, cs, inventoryItem, transaction.getInTransitStored());

		if(inTransit.getType() != StoredType.AMOUNT){
			throw new IllegalArgumentException("In transit stored must be an amount type to receive an amount of it.");
		}

		Quantity<?> inTransitAmount = ((AmountStored)inTransit).getAmount();

		boolean all = false;
		Quantity<?> toReceive;

		if(transaction.isAll()){
			all = true;
			toReceive = inTransitAmount;
		} else {
			toReceive = transaction.getAmount();
		}

		if (toReceive == null) {
			throw new IllegalArgumentException();
		}
		if (!toReceive.getUnit().isCompatible(inventoryItem.getUnit())) {
			throw new IllegalArgumentException();
		}
		if(UnitUtils.isZero(toReceive)){
			throw new IllegalArgumentException("Cannot add a zero amount.");
		}


//		Stored receiving;//TODO:: much complex
//		{
//			ObjectId receivingId = transaction.getToStored();
//			if(receivingId == null){
//				InTransitLocale toLocale = ((InTransit)inTransit.getState()).getTo();
//				receivingId = switch (toLocale.getType()){
//					case STORAGE_BLOCK -> null;
//					case STORED -> null;
//					case GENERIC -> null;
//				}
//			}
//		}







		affectedStored.add(inTransit);
		this.getStoredService().update(oqmDbIdOrName, cs, inTransit, interactingEntity, historyDetails);
	}
}
