package tech.ebp.oqm.baseStation.service.importExport;

import com.mongodb.client.model.Sorts;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.service.TempFileService;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.baseStation.service.mongo.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.ItemCategoryService;
import tech.ebp.oqm.baseStation.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.lib.core.object.interactingEntity.user.User;
import tech.ebp.oqm.lib.core.object.media.Image;
import tech.ebp.oqm.lib.core.object.storage.ItemCategory;
import tech.ebp.oqm.lib.core.object.storage.checkout.CheckInDetails;
import tech.ebp.oqm.lib.core.object.storage.checkout.CheckInState;
import tech.ebp.oqm.lib.core.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.lib.core.object.storage.checkout.checkoutFor.CheckoutForExtUser;
import tech.ebp.oqm.lib.core.object.storage.checkout.checkoutFor.CheckoutForOqmEntity;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.ListAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.TrackedItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.rest.unit.custom.NewBaseCustomUnitRequest;
import tech.ebp.oqm.lib.core.rest.unit.custom.NewDerivedCustomUnitRequest;
import tech.ebp.oqm.lib.core.units.CustomUnitEntry;
import tech.ebp.oqm.lib.core.units.UnitCategory;
import tech.ebp.oqm.lib.core.units.UnitUtils;
import tech.ebp.oqm.lib.core.units.ValidUnitDimension;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class DataImportServiceTest extends RunningServerTest {
	
	@Inject
	DataImportService dataImportService;
	
	@Inject
	DataExportService dataExportService;
	
	@Inject
	TestUserService testUserService;
	
	@Inject
	CustomUnitService customUnitService;
	@Inject
	FileAttachmentService fileAttachmentService;
	@Inject
	ImageService imageService;
	@Inject
	ItemCategoryService itemCategoryService;
	@Inject
	StorageBlockService storageBlockService;
	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	ItemCheckoutService itemCheckoutService;
	@Inject
	TempFileService tempFileService;
	
	@Test
	public void testImportService() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		Random rand = new SecureRandom();
		
		//TODO:: refactor
		// add units
		int unitCount = 0;
		for (int i = 0; i < 5; i++) {
			CustomUnitEntry curImage = new CustomUnitEntry(
				UnitCategory.Number,
				unitCount++,
				new NewBaseCustomUnitRequest(ValidUnitDimension.amount)
					.setUnitCategory(UnitCategory.Number)
					.setName(FAKER.name().name())
					.setSymbol(FAKER.food().dish())
			);
			this.customUnitService.add(curImage, testUser);
		}
		List<CustomUnitEntry> customUnits = this.customUnitService.list();
		UnitUtils.registerAllUnits(customUnits);
		for (int i = 0; i < 5; i++) {
			CustomUnitEntry curImage = new CustomUnitEntry(
				UnitCategory.Number,
				unitCount++,
				new NewDerivedCustomUnitRequest(
					customUnits.get(rand.nextInt(customUnits.size())).getUnitCreator().toUnit(),
					new BigDecimal(rand.nextInt()),
					NewDerivedCustomUnitRequest.DeriveType.multiply
				)
					.setUnitCategory(UnitCategory.Number)
					.setName(FAKER.name().name())
					.setSymbol(FAKER.food().dish())
			);
			this.customUnitService.add(curImage, testUser);
		}
		customUnits = this.customUnitService.list();
		UnitUtils.registerAllUnits(customUnits);
		
		//TODO:: once we have shit figured out for files
//		File tempFilesDir = this.tempFileService.getTempDir("import-test-files", null);
//		for (int i = 0; i < 5; i++) {
//			FileAttachment attachment = new FileAttachment();
//			attachment.setDescription(FAKER.lorem().paragraph());
//
//			File curFile = new File(tempFilesDir, i + "-" + 0 + ".txt");
//
//			FileUtils.writeStringToFile(curFile, FAKER.lorem().paragraph(), Charset.defaultCharset());
//
//
//			ObjectId id = this.fileAttachmentService.add(attachment, curFile, testUser);
//
//			for(int j = 1; j <= 3; j++){
//				curFile = new File(tempFilesDir, i + "-" + j + ".txt");
//				FileUtils.writeStringToFile(curFile, FAKER.lorem().paragraph(), Charset.defaultCharset());
//				this.fileAttachmentService.updateFile(id, curFile, testUser);
//			}
//		}
		
		//Add images
		for (int i = 0; i < 5; i++) {
			Image curImage = new Image();
			curImage.setTitle(FAKER.name().name());
			curImage.setData(Base64.getEncoder().encodeToString("hello world".getBytes()));
			curImage.setType("png");
			curImage.getAttributes().put("key", "val");
			curImage.getKeywords().add("hello world");
			this.imageService.add(curImage, testUser);
		}
		
		//add item category
		List<ObjectId> itemCategoryIds = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			ItemCategory curCategory = new ItemCategory();
			curCategory.setName(FAKER.name().name());
			curCategory.setDescription(FAKER.lorem().paragraph());
			curCategory.getAttributes().put("key", "val");
			curCategory.getKeywords().add("hello world");
			
			if (!itemCategoryIds.isEmpty() && rand.nextBoolean()) {
				curCategory.setParent(itemCategoryIds.get(rand.nextInt(itemCategoryIds.size())));
			}
			
			itemCategoryIds.add(this.itemCategoryService.add(curCategory, testUser));
		}
		//add storage blocks
		List<ObjectId> storageIds = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			StorageBlock storageBlock = new StorageBlock();
			storageBlock.setLabel(FAKER.name().name());
			storageBlock.setNickname(FAKER.name().name());
			storageBlock.setDescription(FAKER.lorem().paragraph());
			storageBlock.setLocation(FAKER.lordOfTheRings().location());
			
			if (!storageIds.isEmpty() && rand.nextBoolean()) {
				storageBlock.setParent(storageIds.get(rand.nextInt(storageIds.size())));
			}
			
			storageBlock.getAttributes().put("key", "val");
			storageBlock.getKeywords().add("hello world");
			storageIds.add(this.storageBlockService.add(storageBlock, testUser));
		}
		//add items
		List<ObjectId> itemIds = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			SimpleAmountItem item = new SimpleAmountItem();
			item.setDescription(FAKER.lorem().paragraph());
			item.setName(FAKER.name().name());
			item.setUnit(customUnits.get(rand.nextInt(customUnits.size())).getUnitCreator().toUnit());
			for (int j = 0; j < 5; j++) {
				item.getStoredForStorage(storageIds.get(rand.nextInt(storageIds.size())))
					.setAmount(rand.nextInt(), item.getUnit())
					.setCondition(rand.nextInt(100))
					.setExpires(LocalDateTime.now().plusDays(rand.nextInt(5)))
					.setConditionNotes(FAKER.lorem().paragraph());
			}
			item.getAttributes().put("key", "val");
			item.getKeywords().add("hello world");
			itemIds.add(this.inventoryItemService.add(item, testUser));
		}
		for (int i = 0; i < 5; i++) {
			ListAmountItem item = new ListAmountItem();
			item.setDescription(FAKER.lorem().paragraph());
			item.setName(FAKER.name().name());
			for (int j = 0; j < 5; j++) {
				item.getStoredForStorage(storageIds.get(rand.nextInt(storageIds.size()))).add(
					(AmountStored) new AmountStored()
									   .setAmount(rand.nextInt(),  item.getUnit())
									   .setCondition(rand.nextInt(100))
									   .setExpires(LocalDateTime.now().plusDays(rand.nextInt(5)))
									   .setConditionNotes(FAKER.lorem().paragraph())
				);
			}
			item.getAttributes().put("key", "val");
			item.getKeywords().add("hello world");
			itemIds.add(this.inventoryItemService.add(item, testUser));
		}
		for (int i = 0; i < 5; i++) {
			TrackedItem item = new TrackedItem();
			item.setDescription(FAKER.lorem().paragraph());
			item.setName(FAKER.name().name());
			item.setTrackedItemIdentifierName("id");
			for (int j = 0; j < 5; j++) {
				item.add(
					storageIds.get(rand.nextInt(storageIds.size())),
					(TrackedStored) new TrackedStored()
										.setIdentifier(FAKER.idNumber().valid())
										.setCondition(rand.nextInt(100))
										.setExpires(LocalDateTime.now().plusDays(rand.nextInt(5)))
										.setConditionNotes(FAKER.lorem().paragraph())
				);
			}
			item.getAttributes().put("key", "val");
			item.getKeywords().add("hello world");
			itemIds.add(this.inventoryItemService.add(item, testUser));
		}
		//add item checkouts
		for (int i = 0; i < 15; i++) {
			ItemCheckout checkout = new ItemCheckout();
			
			//set checkout main data
			checkout.setItem(itemIds.get(rand.nextInt(itemIds.size())));
			InventoryItem itemCheckingOut = this.inventoryItemService.get(checkout.getItem());
			List<ObjectId> storageBlocksInItem = itemCheckingOut
													 .getStorageMap().keySet().stream().toList();
			checkout.setCheckedOutFrom(storageBlocksInItem.get(rand.nextInt(storageBlocksInItem.size())));
			checkout.setCheckedOut(
				switch (itemCheckingOut.getStorageType()){
					case AMOUNT_LIST, AMOUNT_SIMPLE -> new AmountStored(rand.nextInt(), itemCheckingOut.getUnit());
					case TRACKED -> new TrackedStored(FAKER.barcode().gtin14()+"");
				}
			);
			
			checkout.setNotes(FAKER.lorem().paragraph());
			checkout.setReason(FAKER.lorem().paragraph());
			checkout.setDueBack(ZonedDateTime.now().plusDays(rand.nextInt()));
			
			if(rand.nextBoolean()){
				//internal user checked out
				checkout.setCheckedOutFor(
					new CheckoutForOqmEntity(testUser.getReference())
				);
			} else {
				checkout.setCheckedOutFor(
					new CheckoutForExtUser(FAKER.idNumber().valid(), FAKER.name().name())
				);
			}
			
			if(rand.nextBoolean()){
				//checked back in
				checkout.setCheckInDetails(new CheckInDetails(
					CheckInState.values()[rand.nextInt(CheckInState.values().length)],
					storageBlocksInItem.get(rand.nextInt(storageBlocksInItem.size())),
					FAKER.lorem().paragraph(),
					ZonedDateTime.now()
				));
			}
			
			this.itemCheckoutService.add(checkout, testUser);
		}
		File bundle = this.dataExportService.exportDataToBundle(false);
		
		
		
		List<ItemCheckout> oldCheckedout = this.itemCheckoutService.list(null, Sorts.ascending("checkoutDate"), null);
		this.itemCheckoutService.removeAll(testUser);
		this.itemCheckoutService.getHistoryService().removeAll();
		List<InventoryItem> oldItems = this.inventoryItemService.list(null, Sorts.ascending("name"), null);
		this.inventoryItemService.removeAll(testUser);
		this.inventoryItemService.getHistoryService().removeAll();
		List<StorageBlock> oldBlocks = this.storageBlockService.list(null, Sorts.ascending("label"), null);
		this.storageBlockService.removeAll(testUser);
		this.storageBlockService.getHistoryService().removeAll();
		List<Image> oldImages = this.imageService.list(null, Sorts.ascending("title"), null);
		this.imageService.removeAll(testUser);
		this.imageService.getHistoryService().removeAll();
		List<ItemCategory> oldItemCategories = this.itemCategoryService.list(null, Sorts.ascending("name"), null);
		this.itemCategoryService.removeAll(testUser);
		this.itemCategoryService.getHistoryService().removeAll();
		//TODO:: once we have shit figured out for files
//		List<FileAttachmentGet> fileAttachments =
//			this.fileAttachmentService.getFileObjectService().list(null, Sorts.ascending("_id"), null)
//				.stream()
//				.map((FileAttachment a)->{
//					return FileAttachmentGet.fromFileAttachment(a, fileAttachmentService.getRevisions(a.getId()));
//				})
//				.toList();
//		this.fileAttachmentService.removeAll(null, testUser);
//		this.fileAttachmentService.getFileObjectService().getHistoryService().removeAll();
		
		List<CustomUnitEntry> oldUnits = this.customUnitService.list(null, Sorts.ascending("order"), null);
		this.customUnitService.removeAll(testUser);
		this.customUnitService.getHistoryService().removeAll();
		UnitUtils.reInitUnitCollections();
		
		log.info("Size of file bundle: {}", bundle.length());
		
		try(InputStream is = new FileInputStream(bundle)) {
			this.dataImportService.importBundle(is, "test.tar.gz", testUser);
		}
		
		assertEquals(oldUnits.size(), this.customUnitService.list().size());
		assertEquals(oldUnits, this.customUnitService.list(null, Sorts.ascending("order"), null));
		
		assertEquals(oldItems.size(), this.inventoryItemService.list().size());
		assertEquals(oldItems, this.inventoryItemService.list(null, Sorts.ascending("name"), null));
		
		assertEquals(oldBlocks.size(), this.storageBlockService.list().size());
		assertEquals(oldBlocks, this.storageBlockService.list(null, Sorts.ascending("label"), null));
		
		assertEquals(oldImages.size(), this.imageService.list().size());
		assertEquals(oldImages, this.imageService.list(null, Sorts.ascending("title"), null));
		
		assertEquals(oldItemCategories.size(), this.itemCategoryService.list().size());
		assertEquals(oldItemCategories, this.itemCategoryService.list(null, Sorts.ascending("name"), null));
		
		assertEquals(oldCheckedout.size(), this.itemCheckoutService.list().size());
		assertEquals(oldCheckedout, this.itemCheckoutService.list(null, Sorts.ascending("checkoutDate"), null));
		
		//TODO:: verify file attachments once we got that going
	}
	
	//TODO:: test failed import doesn't break things
}