package tech.ebp.oqm.baseStation.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbDeleteRelationalException;
import tech.ebp.oqm.baseStation.testResources.data.ItemCategoryTestObjectCreator;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.lib.core.object.interactingEntity.user.User;
import tech.ebp.oqm.lib.core.object.storage.ItemCategory;
import tech.ebp.oqm.lib.core.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;

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
class ItemCategoryServiceTest extends MongoHistoriedServiceTest<ItemCategory, ItemCategoryService> {
	
	ItemCategoryService itemCategoryService;
	InventoryItemService inventoryItemService;
	StorageBlockService storageBlockService;
	
	ItemCategoryTestObjectCreator itemCategoryTestObjectCreator;
	
	@Inject
	ItemCategoryServiceTest(
		ItemCategoryService itemCategoryService,
		ItemCategoryTestObjectCreator itemCategoryTestObjectCreator,
		TestUserService testUserService,
		InventoryItemService inventoryItemService,
		StorageBlockService storageBlockService
	) {
		this.itemCategoryService = itemCategoryService;
		this.itemCategoryTestObjectCreator = itemCategoryTestObjectCreator;
		this.testUserService = testUserService;
		
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
		User testUser = this.testUserService.getTestUser();
		ItemCategory itemCategory = this.getTestObject();
		Map<String, Set<ObjectId>> expectedRefs = new HashMap<>();
		
		this.itemCategoryService.add(itemCategory, testUser);
		{//setup referencing data
			ItemCategory subCategory = this.getTestObject();
			subCategory.setParent(itemCategory.getId());
			ObjectId catId = this.itemCategoryService.add(subCategory, testUser);
			expectedRefs.put(this.itemCategoryService.getClazz().getSimpleName(), new TreeSet<>(List.of(catId)));
			
			//Inventory item, basic
			this.inventoryItemService.add(new SimpleAmountItem().setName(FAKER.name().name()).setCategories(List.of(ObjectId.get())), testUser);
			
			SimpleAmountItem sai = (SimpleAmountItem) new SimpleAmountItem().setName(FAKER.name().name());
			sai.setCategories(List.of(itemCategory.getId()));
			ObjectId itemId = this.inventoryItemService.add(sai, testUser);
			expectedRefs.put(this.inventoryItemService.getClazz().getSimpleName(), new TreeSet<>(List.of(itemId)));
			//Storage Block
			this.storageBlockService.add(new StorageBlock().setLabel(FAKER.name().fullName()), testUser);
			
			StorageBlock storageBlock =new StorageBlock().setLabel(FAKER.name().fullName());
			storageBlock.setStoredCategories(List.of(itemCategory.getId()));
			itemId = this.storageBlockService.add(storageBlock, testUser);
			expectedRefs.put(this.storageBlockService.getClazz().getSimpleName(), new TreeSet<>(List.of(itemId)));
		}
		
		DbDeleteRelationalException exception = assertThrows(
			DbDeleteRelationalException.class,
			()->this.itemCategoryService.remove(itemCategory.getId(), testUser)
		);
		
		log.info("Referenced objects: {}", exception.getObjectsReferencing());
		assertEquals(expectedRefs, exception.getObjectsReferencing());
	}
}