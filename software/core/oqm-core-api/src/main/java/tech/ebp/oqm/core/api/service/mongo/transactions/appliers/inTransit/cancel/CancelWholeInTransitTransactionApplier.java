package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.cancel;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
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
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.ReturnableLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StorageBlockLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StoredLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.cancel.CancelInTransitWholeTransaction;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitTransactionApplier;

import java.util.List;
import java.util.Set;

/**
 * Applier to handle AddAmountTransactions.
 *
 */
@Slf4j
@ApplicationScoped
public class CancelWholeInTransitTransactionApplier extends InTransitTransactionApplier<CancelInTransitWholeTransaction> {

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.CANCEL_IN_TRANSIT_WHOLE;
	}

	private void returnToStored(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		ClientSession cs,
		Set<Stored> affectedStored,
		InteractingEntity interactingEntity,
		HistoryDetail[] historyDetails,
		Stored inTransit,
		Stored toStored
	){
		if(inTransit.getType() != StoredType.AMOUNT){
			throw new IllegalArgumentException("Cannot return one non-amount stored to another.");
		}
		if(toStored.getType() != StoredType.AMOUNT){
			throw new IllegalArgumentException("Cannot return amount stored to a non-amount stored.");
		}

		affectedStored.add(toStored);
		((AmountStored)toStored).add(((AmountStored)inTransit).getAmount());

		this.getStoredService().update(oqmDbIdOrName, cs, toStored, interactingEntity, historyDetails);











	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		CancelInTransitWholeTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		Stored stored = this.getStoredService().get(oqmDbIdOrName, cs, transaction.getInTransitStored());

		if (!inventoryItem.getId().equals(stored.getItem())) {
			throw new IllegalArgumentException("Stored item's associated item must match concerning item.");
		}
		if(stored.getState().getType() != StoredStateType.IN_TRANSIT){
			throw new IllegalArgumentException("Stored item is not in transit.");
		}

		boolean remove = false;
		switch(transaction.getCancelType()){
			case CANCEL -> {
				remove = true;
			}
			case RETURN -> {//return back to "from"
				ReturnableLocale locale = transaction.getReturnTo();

				if(locale == null){
					InTransitLocale from = ((InTransit)stored.getState()).getFrom();

					if(!(from instanceof ReturnableLocale)){
						throw new IllegalArgumentException("Stored in transit item not from a returnable location.");
					}
					locale = (ReturnableLocale) from;
				}

				switch(locale.getType()){
					case STORAGE_BLOCK -> {
						ObjectId toBlock = ((StorageBlockLocale)locale).getStorageBlock();

						if(!inventoryItem.usesStorageBlock(toBlock)){
							throw new IllegalArgumentException("Cannot return stored to block that item is not stored in.");
						}

						switch(inventoryItem.getStorageType()){
							case BULK:

								Stored bulkExisting = this.getStoredService().listIterator(
									oqmDbIdOrName,
									cs,
									new StoredSearch()
										.setInventoryItemId(inventoryItem.getId())
										.setInStorageBlocks(List.of(toBlock))
								).first();

								if(bulkExisting == null){
									stored.setState(
										StoredInBlock.builder()
											.storageBlock(toBlock)
											.build()
									);
								} else {
									remove = true;
									this.returnToStored(
										oqmDbIdOrName,
										inventoryItem,
										cs,
										affectedStored,
										interactingEntity,
										historyDetails,
										stored,
										bulkExisting
									);
								}
								break;
							case AMOUNT_LIST:
							case UNIQUE_MULTI:
							case UNIQUE_SINGLE:
								stored.setState(
									StoredInBlock.builder()
										.storageBlock(toBlock)
										.build()
								);
								break;
						}
					}
					case STORED -> {
						remove = true;
						this.returnToStored(
							oqmDbIdOrName,
							inventoryItem,
							cs,
							affectedStored,
							interactingEntity,
							historyDetails,
							stored,
							this.getStoredService().get(oqmDbIdOrName, cs, ((StoredLocale)locale).getStored())
						);
					}
					default -> throw new IllegalStateException("Cannot return to a generic locale.");
				}
			}
		}

		affectedStored.add(stored);
		if(remove){
			this.getStoredService().remove(oqmDbIdOrName, cs, stored.getId(), interactingEntity, historyDetails);
		} else {
			this.getStoredService().update(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
		}

	}
}
