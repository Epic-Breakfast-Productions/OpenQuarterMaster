package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.receive;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.GenericLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitAppliedTransactionTest;
import tech.units.indriya.quantity.Quantities;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.ebp.oqm.core.api.model.object.history.details.HistoryDetailType.ITEM_TRANSACTION;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
@QuarkusTestResource(value = KafkaCompanionResource.class, restrictToAnnotatedClass = true)
public class ReceiveAmountInTransitAppliedTransactionTest extends InTransitAppliedTransactionTest {


	//TODO:: bulk, add all (flag)
	//TODO:: bulk, add all (quantity)
	//TODO:: bulk, add all from "to" (flag)
	//TODO:: bulk, add all from "to" (quantity)
	//TODO:: bulk, add part
	//TODO:: amount list, add all to stored (flag)
	//TODO:: amount list, add all to block (quantity)
	//TODO:: amount list, add all to stored from "to" (flag)
	//TODO:: amount list, add all to block from "to" (quantity)
}
