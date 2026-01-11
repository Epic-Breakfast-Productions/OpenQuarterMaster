package tech.ebp.oqm.core.api.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.exception.db.DbDeleteRelationalException;
import tech.ebp.oqm.core.api.testResources.data.StorageBlockTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;

import jakarta.inject.Inject;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

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
		InventoryItemService inventoryItemService
	) {
		this.storageBlockService = storageBlockService;
		this.storageBlockTestObjectCreator = storageBlockTestObjectCreator;
		
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
		User testUser = this.getTestUserService().getTestUser();
		StorageBlock storageBlock = this.getTestObject();
		Map<String, Set<ObjectId>> expectedRefs = new HashMap<>();
		
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, storageBlock, testUser);
		{//setup referencing data
			//parent
			StorageBlock subBlock = this.getTestObject();
			subBlock.setParent(storageBlock.getId());
			ObjectId subBlockId = this.storageBlockService.add(DEFAULT_TEST_DB_NAME, subBlock, testUser).getId();
			expectedRefs.put(this.storageBlockService.getClazz().getSimpleName(), new TreeSet<>(List.of(subBlockId)));
			
			//Inventory item, basic
			this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, InventoryItem.builder().name(FAKER.name().name()).storageType(StorageType.BULK).build(), testUser);

			InventoryItem sai = InventoryItem.builder().name(FAKER.name().name()).storageType(StorageType.BULK).storageBlocks(
				new LinkedHashSet<>(List.of(storageBlock.getId()))
			).build();

			ObjectId itemId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, sai, testUser).getId();
			expectedRefs.put(this.inventoryItemService.getClazz().getSimpleName(), new TreeSet<>(List.of(itemId)));
		}
		
		DbDeleteRelationalException exception = assertThrows(
			DbDeleteRelationalException.class,
			()->this.storageBlockService.remove(DEFAULT_TEST_DB_NAME, storageBlock.getId(), testUser)
		);
		
		log.info("Referenced objects: {}", exception.getObjectsReferencing());
		assertEquals(expectedRefs, exception.getObjectsReferencing());
	}
}