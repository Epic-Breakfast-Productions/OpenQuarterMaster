package tech.ebp.oqm.baseStation.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbDeleteRelationalException;
import tech.ebp.oqm.baseStation.testResources.data.ImageTestObjectCreator;
import tech.ebp.oqm.baseStation.testResources.data.StorageBlockTestObjectCreator;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.object.media.Image;
import tech.ebp.oqm.baseStation.model.object.storage.ItemCategory;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.ListAmountItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.TrackedItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.baseStation.model.units.OqmProvidedUnits;
import tech.units.indriya.quantity.Quantities;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class StorageBlockServiceTest extends MongoHistoriedServiceTest<StorageBlock, StorageBlockService> {
	
	StorageBlockService storageBlockService;
	InventoryItemService inventoryItemService;
	
	StorageBlockTestObjectCreator storageBlockTestObjectCreator;
	
	@Inject
	StorageBlockServiceTest(
		StorageBlockService storageBlockService,
		StorageBlockTestObjectCreator storageBlockTestObjectCreator,
		TestUserService testUserService,
		InventoryItemService inventoryItemService
	) {
		this.storageBlockService = storageBlockService;
		this.storageBlockTestObjectCreator = storageBlockTestObjectCreator;
		this.testUserService = testUserService;
		
		this.inventoryItemService = inventoryItemService;
	}
	
	@Override
	protected StorageBlock getTestObject() {
		return storageBlockTestObjectCreator.getTestObject();
	}
	
	@Test
	public void injectTest() {
		assertNotNull(storageBlockService);
	}
	
	@Test
	public void listTest() {
		this.defaultListTest(this.storageBlockService);
	}
	
	@Test
	public void countTest() {
		this.defaultCountTest(this.storageBlockService);
	}
	
	@Test
	public void addTest() {
		this.defaultAddTest(this.storageBlockService);
	}
	
	//TODO:: Test update
	
	@Test
	public void getObjectIdTest() {
		this.defaultGetObjectIdTest(this.storageBlockService);
	}
	
	@Test
	public void getStringTest() {
		this.defaultGetStringTest(this.storageBlockService);
	}
	
	@Test
	public void removeAllTest() {
		this.defaultRemoveAllTest(this.storageBlockService);
	}
	
	@Ignore
	@Test
	public void testDeleteWithRelational(){
		User testUser = this.testUserService.getTestUser();
		StorageBlock storageBlock = this.getTestObject();
		Map<String, Set<ObjectId>> expectedRefs = new HashMap<>();
		
		this.storageBlockService.add(storageBlock, testUser);
		{//setup referencing data
			//parent
			StorageBlock subBlock = this.getTestObject();
			subBlock.setParent(storageBlock.getId());
			ObjectId subBlockId = this.storageBlockService.add(subBlock, testUser);
			expectedRefs.put(this.storageBlockService.getClazz().getSimpleName(), new TreeSet<>(List.of(subBlockId)));
			
			//Inventory item, basic
			this.inventoryItemService.add(new SimpleAmountItem().setName(FAKER.name().name()), testUser);
			
			SimpleAmountItem sai = (SimpleAmountItem) new SimpleAmountItem().setName(FAKER.name().name());
			sai.getStoredForStorage(storageBlock.getId());
			ObjectId itemId = this.inventoryItemService.add(sai, testUser);
			expectedRefs.put(this.inventoryItemService.getClazz().getSimpleName(), new TreeSet<>(List.of(itemId)));
		}
		
		DbDeleteRelationalException exception = assertThrows(
			DbDeleteRelationalException.class,
			()->this.storageBlockService.remove(storageBlock.getId(), testUser)
		);
		
		log.info("Referenced objects: {}", exception.getObjectsReferencing());
		assertEquals(expectedRefs, exception.getObjectsReferencing());
	}
}