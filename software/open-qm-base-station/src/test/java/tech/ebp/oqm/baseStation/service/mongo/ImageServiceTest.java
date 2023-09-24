package tech.ebp.oqm.baseStation.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbDeleteRelationalException;
import tech.ebp.oqm.baseStation.testResources.data.ImageTestObjectCreator;
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
import tech.ebp.oqm.baseStation.model.units.UnitUtils;
import tech.units.indriya.quantity.Quantities;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class ImageServiceTest extends MongoHistoriedServiceTest<Image, ImageService> {
	
	ImageService imageService;
	StorageBlockService storageBlockService;
	ItemCategoryService itemCategoryService;
	InventoryItemService inventoryItemService;
	
	ImageTestObjectCreator imageTestObjectCreator;
	
	@Inject
	ImageServiceTest(
		ImageService imageService,
		ImageTestObjectCreator imageTestObjectCreator,
		TestUserService testUserService,
		StorageBlockService storageBlockService,
		ItemCategoryService itemCategoryService,
		InventoryItemService inventoryItemService
	) {
		this.imageService = imageService;
		this.imageTestObjectCreator = imageTestObjectCreator;
		this.testUserService = testUserService;
		
		this.storageBlockService = storageBlockService;
		this.itemCategoryService = itemCategoryService;
		this.inventoryItemService = inventoryItemService;
	}
	
	@Override
	protected Image getTestObject() {
		return imageTestObjectCreator.getTestObject();
	}
	
	@Test
	public void injectTest() {
		assertNotNull(imageService);
	}
	
	@Test
	public void listTest() {
		this.defaultListTest(this.imageService);
	}
	
	@Test
	public void countTest() {
		this.defaultCountTest(this.imageService);
	}
	
	@Test
	public void addTest() {
		this.defaultAddTest(this.imageService);
	}
	
	//TODO:: Test update
	
	@Test
	public void getObjectIdTest() {
		this.defaultGetObjectIdTest(this.imageService);
	}
	
	@Test
	public void getStringTest() {
		this.defaultGetStringTest(this.imageService);
	}
	
	@Test
	public void removeAllTest() {
		this.defaultRemoveAllTest(this.imageService);
	}
	
	@Ignore
	@Test
	public void testDeleteWithRelational(){
		User testUser = this.testUserService.getTestUser();
		Image testImage = this.getTestObject();
		Map<String, Set<ObjectId>> expectedRefs = new HashMap<>();
		
		this.imageService.add(testImage, testUser);
		{//setup referencing data
			//Storage block
			ObjectId storageBlockId = this.storageBlockService.add((StorageBlock) new StorageBlock().setLabel(FAKER.name().fullName()).setImageIds(List.of(testImage.getId(), ObjectId.get())), testUser);
			this.storageBlockService.add((StorageBlock) new StorageBlock().setLabel(FAKER.name().fullName()).setImageIds(List.of(ObjectId.get())), testUser);
			expectedRefs.put(this.storageBlockService.getClazz().getSimpleName(), new TreeSet<>(List.of(storageBlockId)));
			
			//Item Category
			ObjectId itemCatId = this.itemCategoryService.add((ItemCategory) new ItemCategory().setName(FAKER.name().name()).setImageIds(List.of(testImage.getId(), ObjectId.get())), testUser);
			this.itemCategoryService.add(new ItemCategory().setName(FAKER.name().name()), testUser);
			expectedRefs.put(this.itemCategoryService.getClazz().getSimpleName(), new TreeSet<>(List.of(itemCatId)));
			
			//Inventory item, basic
			this.inventoryItemService.add((InventoryItem<?,?,?>) new SimpleAmountItem().setName(FAKER.name().name()).setImageIds(List.of(ObjectId.get())), testUser);
			
			ObjectId itemId = this.inventoryItemService.add((InventoryItem<?,?,?>) new SimpleAmountItem().setName(FAKER.name().name()).setImageIds(List.of(testImage.getId(), ObjectId.get())), testUser);
			expectedRefs.put(this.inventoryItemService.getClazz().getSimpleName(), new TreeSet<>(List.of(itemId)));
			
			{//In stored
				SimpleAmountItem sai = (SimpleAmountItem) new SimpleAmountItem().setName(FAKER.name().name());
				sai.getStoredForStorage(storageBlockId).setImageIds(List.of(
					testImage.getId(),
					ObjectId.get()
				));
				itemId = this.inventoryItemService.add(sai, testUser);
				expectedRefs.get(this.inventoryItemService.getClazz().getSimpleName()).add(itemId);
			}
			{//In list
				ListAmountItem lai = (ListAmountItem) new ListAmountItem().setName(FAKER.name().name());
				lai.getStoredForStorage(storageBlockId)
					.add((AmountStored) new AmountStored(Quantities.getQuantity(0, OqmProvidedUnits.UNIT)).setImageIds(List.of(testImage.getId(), ObjectId.get())));
				itemId = this.inventoryItemService.add(lai, testUser);
				expectedRefs.get(this.inventoryItemService.getClazz().getSimpleName()).add(itemId);
			}
			{//In tracked
				TrackedItem ti = (TrackedItem) new TrackedItem().setTrackedItemIdentifierName("sid").setName(FAKER.name().name());
				ti.getStoredForStorage(storageBlockId).put("Identifier", (TrackedStored) new TrackedStored("Identifier").setImageIds(List.of(testImage.getId(), ObjectId.get())));
				itemId = this.inventoryItemService.add(ti, testUser);
				expectedRefs.get(this.inventoryItemService.getClazz().getSimpleName()).add(itemId);
			}
		}
		
		DbDeleteRelationalException exception = assertThrows(
			DbDeleteRelationalException.class,
			()->this.imageService.remove(testImage.getId(), testUser)
		);
		
		log.info("Referenced objects: {}", exception.getObjectsReferencing());
		assertEquals(expectedRefs, exception.getObjectsReferencing());
	}
}