package tech.ebp.oqm.core.api.service.importExport;

import com.mongodb.client.model.Sorts;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.rest.media.ImageGet;
import tech.ebp.oqm.core.api.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.core.api.service.TempFileService;
import tech.ebp.oqm.core.api.service.importExport.exporting.DataExportOptions;
import tech.ebp.oqm.core.api.service.importExport.exporting.DatabaseExportService;
import tech.ebp.oqm.core.api.service.importExport.importing.DataImportService;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.mongo.CustomUnitService;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.service.mongo.InteractingEntityService;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.ItemCategoryService;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.core.api.testResources.RetryRule;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutForOqmEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.ListAmountItem;
import tech.ebp.oqm.core.api.model.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.core.api.model.object.storage.items.TrackedItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.storage.itemCheckout.ItemCheckoutRequest;
import tech.ebp.oqm.core.api.model.rest.unit.custom.NewBaseCustomUnitRequest;
import tech.ebp.oqm.core.api.model.rest.unit.custom.NewDerivedCustomUnitRequest;
import tech.ebp.oqm.core.api.model.units.CustomUnitEntry;
import tech.ebp.oqm.core.api.model.units.UnitCategory;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.model.units.ValidUnitDimension;

import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class DataImportServiceTest extends RunningServerTest {
	
	@Inject
	DataImportService dataImportService;
	
	@Inject
	DatabaseExportService databaseExportService;
	
	@Inject
	TestUserService testUserService;
	
	@Inject
	InteractingEntityService interactingEntityService;
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
	
	@Rule
	public RetryRule retryRule = new RetryRule(3);
	
	@Test
	public void testImportService() throws IOException {
		User testUser = testUserService.getTestUser(true);
		this.interactingEntityService.add(testUser);
		Random rand = new SecureRandom();
		
		//TODO:: refactor
		
		// add units
		int unitCount = 0;
		for (int i = 0; i < 5; i++) {
			CustomUnitEntry customUnitEntry = new CustomUnitEntry(
				UnitCategory.Number,
				unitCount++,
				new NewBaseCustomUnitRequest(ValidUnitDimension.amount)
					.setUnitCategory(UnitCategory.Number)
					.setName(FAKER.name().name())
					.setSymbol(FAKER.food().dish())
			);
			this.customUnitService.add(null, customUnitEntry);
		}
		List<CustomUnitEntry> customUnits = this.customUnitService.list();
		UnitUtils.registerAllUnits(customUnits);
		for (int i = 0; i < 5; i++) {
			CustomUnitEntry customUnitEntry = new CustomUnitEntry(
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
			this.customUnitService.add(null, customUnitEntry);
		}
		customUnits = this.customUnitService.list();
		UnitUtils.registerAllUnits(customUnits);
		
		File tempFilesDir = this.tempFileService.getTempDir("import-test-files", null);
		for (int i = 0; i < 5; i++) {
			FileAttachment attachment = new FileAttachment();
			attachment.setDescription(FAKER.lorem().paragraph());
			attachment.getAttributes().put("key", "val");
			attachment.getKeywords().add("hello world");

			File curFile = new File(tempFilesDir, i + "-" + 0 + ".txt");

			FileUtils.writeStringToFile(curFile, FAKER.lorem().paragraph(), Charset.defaultCharset());
			
			ObjectId id = this.fileAttachmentService.add(DEFAULT_TEST_DB_NAME, attachment, curFile, testUser);

			for(int j = 1; j <= 3; j++){
				curFile = new File(tempFilesDir, i + "-" + j + ".txt");
				FileUtils.writeStringToFile(curFile, FAKER.lorem().paragraph(), Charset.defaultCharset());
				this.fileAttachmentService.updateFile(DEFAULT_TEST_DB_NAME, id, curFile, testUser);
			}
		}
		
		//Add images
		for (int i = 0; i < 5; i++) {
			Image curImage = new Image();
			curImage.setDescription(FAKER.lorem().paragraph());
			curImage.getAttributes().put("key", "val");
			curImage.getKeywords().add("hello world");
			
			File imageFile = new File(DataImportServiceTest.class.getResource("/test_image.png").getFile());
			
			this.imageService.add(DEFAULT_TEST_DB_NAME, curImage, imageFile, testUser);
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
			
			itemCategoryIds.add(this.itemCategoryService.add(DEFAULT_TEST_DB_NAME, curCategory, testUser));
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
			storageIds.add(this.storageBlockService.add(DEFAULT_TEST_DB_NAME, storageBlock, testUser));
		}
		//add items
		List<ObjectId> itemIds = new ArrayList<>();
		List<SimpleAmountItem> simpleAmountItems = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			SimpleAmountItem item = new SimpleAmountItem();
			item.setDescription(FAKER.lorem().paragraph());
			item.setName(FAKER.name().name());
			item.setUnit(customUnits.get(rand.nextInt(customUnits.size())).getUnitCreator().toUnit());
			for (int j = 0; j < 5; j++) {
				item.getStoredForStorage(storageIds.get(rand.nextInt(storageIds.size())))
					.setAmount(abs(rand.nextInt()), item.getUnit())
					.setCondition(rand.nextInt(100))
					.setExpires(LocalDateTime.now().plusDays(rand.nextInt(5)))
					.setConditionNotes(FAKER.lorem().paragraph());
			}
			item.getAttributes().put("key", "val");
			item.getKeywords().add("hello world");
			ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, testUser);
			itemIds.add(newId);
			simpleAmountItems.add(item);
		}
		List<ListAmountItem> listAmountItems = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			ListAmountItem item = new ListAmountItem();
			item.setDescription(FAKER.lorem().paragraph());
			item.setName(FAKER.name().name());
			for (int j = 0; j < 5; j++) {
				item.getStoredForStorage(storageIds.get(rand.nextInt(storageIds.size()))).add(
					(AmountStored) new AmountStored()
									   .setAmount(abs(rand.nextInt()), item.getUnit())
									   .setCondition(rand.nextInt(100))
									   .setExpires(LocalDateTime.now().plusDays(rand.nextInt(5)))
									   .setConditionNotes(FAKER.lorem().paragraph())
				);
			}
			item.getAttributes().put("key", "val");
			item.getKeywords().add("hello world");
			itemIds.add(this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, testUser));
			listAmountItems.add(item);
		}
		List<TrackedItem> trackedItems = new ArrayList<>();
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
			ObjectId newId =this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, testUser);
			itemIds.add(newId);
			trackedItems.add(item);
		}
		//add item checkouts
		for (int i = 0; i < 1; i++) {
			{
				SimpleAmountItem checkingOutItem = simpleAmountItems.get(rand.nextInt(simpleAmountItems.size()));
				
				LinkedList<Map.Entry<ObjectId, SingleAmountStoredWrapper>> storedEntries = new LinkedList<>(checkingOutItem.getStorageMap().entrySet());
				
				Map.Entry<ObjectId, SingleAmountStoredWrapper> checkingOutEntry = storedEntries.removeFirst();
				ObjectId checkoutId = this.itemCheckoutService.checkoutItem(
					DEFAULT_TEST_DB_NAME,
					ItemCheckoutRequest.builder()
						.item(checkingOutItem.getId())
						.checkedOutFrom(checkingOutEntry.getKey())
						.toCheckout(checkingOutEntry.getValue().getStored())
						.checkedOutFor(new CheckoutForOqmEntity(testUser.getId()))
						.reason(FAKER.lorem().paragraph())
						.notes(FAKER.lorem().paragraph())
						.build(),
					testUser
				);
			}
			//TODO:: rest of item types
		}
		File bundle = this.databaseExportService.exportDataToBundle(DataExportOptions.builder().build());
		
		FileUtils.copyFile(bundle, new File("build/export.tar.gz"));
		
		
		List<ItemCheckout> oldCheckedout = this.itemCheckoutService.list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("checkoutDate"), null);
		this.itemCheckoutService.removeAll(DEFAULT_TEST_DB_NAME, testUser);
		this.itemCheckoutService.getHistoryService().removeAll(DEFAULT_TEST_DB_NAME);
		List<InventoryItem> oldItems = this.inventoryItemService.list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("name"), null);
		this.inventoryItemService.removeAll(DEFAULT_TEST_DB_NAME, testUser);
		this.inventoryItemService.getHistoryService().removeAll(DEFAULT_TEST_DB_NAME);
		List<StorageBlock> oldBlocks = this.storageBlockService.list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("label"), null);
		this.storageBlockService.removeAll(DEFAULT_TEST_DB_NAME, testUser);
		this.storageBlockService.getHistoryService().removeAll(DEFAULT_TEST_DB_NAME);
		List<ImageGet> oldImages =
			this.imageService.getFileObjectService().list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("_id"), null)
				.stream()
				.map((Image a)->{
					return imageService.fileObjToGet(DEFAULT_TEST_DB_NAME, a);
				})
				.toList();
		this.imageService.removeAll(DEFAULT_TEST_DB_NAME, null, testUser);
		this.imageService.getFileObjectService().getHistoryService().removeAll(DEFAULT_TEST_DB_NAME);
		List<ItemCategory> oldItemCategories = this.itemCategoryService.list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("name"), null);
		this.itemCategoryService.removeAll(DEFAULT_TEST_DB_NAME, testUser);
		this.itemCategoryService.getHistoryService().removeAll(DEFAULT_TEST_DB_NAME);
		List<FileAttachmentGet> fileAttachments =
			this.fileAttachmentService.getFileObjectService().list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("_id"), null)
				.stream()
				.map((FileAttachment a)->{
					return FileAttachmentGet.fromFileAttachment(a, fileAttachmentService.getRevisions(DEFAULT_TEST_DB_NAME, a.getId()));
				})
				.toList();
		this.fileAttachmentService.removeAll(DEFAULT_TEST_DB_NAME, null, testUser);
		this.fileAttachmentService.getFileObjectService().getHistoryService().removeAll(DEFAULT_TEST_DB_NAME);
		
//		List<CustomUnitEntry> oldUnits = this.customUnitService.list(null, Sorts.ascending("order"), null);
		List<CustomUnitEntry> oldUnits = this.customUnitService.list();
		this.customUnitService.removeAll();
		UnitUtils.reInitUnitCollections();
		
		log.info("Size of file bundle: {}", bundle.length());
		
		try(InputStream is = new FileInputStream(bundle)) {
			this.dataImportService.importBundle(is, "test.tar.gz", testUser, DataImportOptions.builder().build());
		}
		
		assertEquals(oldUnits.size(), this.customUnitService.list().size());
		assertEquals(oldUnits, this.customUnitService.list());
		
		assertEquals(oldItems.size(), this.inventoryItemService.list(DEFAULT_TEST_DB_NAME).size());
		assertEquals(oldItems, this.inventoryItemService.list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("name"), null));
		
		assertEquals(oldBlocks.size(), this.storageBlockService.list(DEFAULT_TEST_DB_NAME).size());
		assertEquals(oldBlocks, this.storageBlockService.list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("label"), null));
		
		
		assertEquals(oldItemCategories.size(), this.itemCategoryService.list(DEFAULT_TEST_DB_NAME).size());
		assertEquals(oldItemCategories, this.itemCategoryService.list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("name"), null));
		
		assertEquals(oldCheckedout.size(), this.itemCheckoutService.list(DEFAULT_TEST_DB_NAME).size());
		assertEquals(oldCheckedout, this.itemCheckoutService.list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("checkoutDate"), null));
		
		//TODO:: verify file attachments, images
//		assertEquals(oldImages.size(), this.imageService.list().size());
//		assertEquals(oldImages, this.imageService.list(null, Sorts.ascending("title"), null));

//		assertEquals(fileAttachments, this.fileAttachmentService.getFileObjectService().list(null, Sorts.ascending("_id"), null)
//										  .stream()
//										  .map((FileAttachment a)->{
//											  return FileAttachmentGet.fromFileAttachment(a, fileAttachmentService.getRevisions(a.getId()));
//										  })
//										  .toList());
	}
	
	//TODO:: test failed import doesn't break things
}