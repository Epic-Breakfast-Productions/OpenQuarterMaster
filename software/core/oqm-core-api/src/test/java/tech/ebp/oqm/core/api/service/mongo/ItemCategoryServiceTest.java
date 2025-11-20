package tech.ebp.oqm.core.api.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.exception.db.DbDeleteRelationalException;
import tech.ebp.oqm.core.api.testResources.data.ItemCategoryTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class ItemCategoryServiceTest extends MongoHistoriedServiceTest<ItemCategory, ItemCategoryService> {
	
	ItemCategoryService itemCategoryService;
	InventoryItemService inventoryItemService;
	StorageBlockService storageBlockService;
	
	ItemCategoryTestObjectCreator itemCategoryTestObjectCreator;
	
	@Inject
	ItemCategoryServiceTest(
		ItemCategoryService itemCategoryService,
		ItemCategoryTestObjectCreator itemCategoryTestObjectCreator,
		InventoryItemService inventoryItemService,
		StorageBlockService storageBlockService
	) {
		this.itemCategoryService = itemCategoryService;
		this.itemCategoryTestObjectCreator = itemCategoryTestObjectCreator;
		
		this.inventoryItemService = inventoryItemService;
		this.storageBlockService = storageBlockService;
	}
	
	@Override
	protected ItemCategory getTestObject() {
		return itemCategoryTestObjectCreator.getTestObject();
	}
	
	@Test
	public void injectTest() {
		assertNotNull(itemCategoryService);
	}
	
	@Test
	public void listTest() {
		this.defaultListTest(this.itemCategoryService);
	}
	
	@Test
	public void countTest() {
		this.defaultCountTest(this.itemCategoryService);
	}
	
	@Test
	public void addTest() {
		this.defaultAddTest(this.itemCategoryService);
	}
	
	//TODO:: Test update
	
	@Test
	public void getObjectIdTest() {
		this.defaultGetObjectIdTest(this.itemCategoryService);
	}
	
	@Test
	public void getStringTest() {
		this.defaultGetStringTest(this.itemCategoryService);
	}
	
	@Test
	public void removeAllTest() {
		this.defaultRemoveAllTest(this.itemCategoryService);
	}
	
	@Ignore
	@Test
	public void testDeleteWithRelational(){
		User testUser = this.getTestUserService().getTestUser();
		ItemCategory itemCategory = this.getTestObject();
		Map<String, Set<ObjectId>> expectedRefs = new HashMap<>();
		
		this.itemCategoryService.add(DEFAULT_TEST_DB_NAME, itemCategory, testUser);
		{//setup referencing data
			ItemCategory subCategory = this.getTestObject();
			subCategory.setParent(itemCategory.getId());
			ObjectId catId = this.itemCategoryService.add(DEFAULT_TEST_DB_NAME, subCategory, testUser);
			expectedRefs.put(this.itemCategoryService.getClazz().getSimpleName(), new TreeSet<>(List.of(catId)));
			
			//Inventory item, basic
			this.inventoryItemService.add(
				DEFAULT_TEST_DB_NAME,
				InventoryItem.builder()
					.storageType(StorageType.BULK)
					.name(FAKER.name().name())
					.build(),
				testUser
			);

			InventoryItem sai = InventoryItem.builder().name(FAKER.name().name()).storageType(StorageType.BULK).unit(OqmProvidedUnits.UNIT).build();
			sai.setCategories(List.of(itemCategory.getId()));
			ObjectId itemId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, sai, testUser);
			expectedRefs.put(this.inventoryItemService.getClazz().getSimpleName(), new TreeSet<>(List.of(itemId)));
			//Storage Block
			this.storageBlockService.add(DEFAULT_TEST_DB_NAME, new StorageBlock().setLabel(FAKER.name().fullName()), testUser);
			
			StorageBlock storageBlock =new StorageBlock().setLabel(FAKER.name().fullName());
			storageBlock.setStoredCategories(List.of(itemCategory.getId()));
			itemId = this.storageBlockService.add(DEFAULT_TEST_DB_NAME, storageBlock, testUser);
			expectedRefs.put(this.storageBlockService.getClazz().getSimpleName(), new TreeSet<>(List.of(itemId)));
		}
		
		DbDeleteRelationalException exception = assertThrows(
			DbDeleteRelationalException.class,
			()->this.itemCategoryService.remove(DEFAULT_TEST_DB_NAME, itemCategory.getId(), testUser)
		);
		
		log.info("Referenced objects: {}", exception.getObjectsReferencing());
		assertEquals(expectedRefs, exception.getObjectsReferencing());
	}
}