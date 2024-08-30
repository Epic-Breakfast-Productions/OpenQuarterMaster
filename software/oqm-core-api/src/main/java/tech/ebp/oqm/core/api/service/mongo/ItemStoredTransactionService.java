package tech.ebp.oqm.core.api.service.mongo;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.rest.search.AppliedTransactionSearch;
import tech.ebp.oqm.core.api.model.rest.search.InteractingEntitySearch;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
@Named("ItemStoredTransactionService")
@ApplicationScoped
public class ItemStoredTransactionService extends MongoObjectService<AppliedTransaction, AppliedTransactionSearch, CollectionStats> {

	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	StoredService storedService;

	public ItemStoredTransactionService() {
		super(AppliedTransaction.class);
	}


	public ObjectId apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		ItemStoredTransaction itemStoredTransaction
	){
		AppliedTransaction newTransaction = new AppliedTransaction();
		newTransaction.setInventoryItem(inventoryItem.getId());

		//TODO

		ObjectId newId = this.add(oqmDbIdOrName, newTransaction);
		return newId;
	}

	public ObjectId apply(
		String oqmDbIdOrName,
		ObjectId inventoryItem,
		ItemStoredTransaction itemStoredTransaction
	){
		return this.apply(oqmDbIdOrName, this.inventoryItemService.get(oqmDbIdOrName, inventoryItem), itemStoredTransaction);
	}

	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return null;
	}
}
