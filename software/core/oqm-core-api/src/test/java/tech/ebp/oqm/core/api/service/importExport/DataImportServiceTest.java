package tech.ebp.oqm.core.api.service.importExport;

import com.mongodb.client.model.Sorts;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.rest.media.ImageGet;
import tech.ebp.oqm.core.api.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.core.api.service.TempFileService;
import tech.ebp.oqm.core.api.service.importExport.exporting.DataExportOptions;
import tech.ebp.oqm.core.api.service.importExport.exporting.DatabaseExportService;
import tech.ebp.oqm.core.api.service.importExport.importing.DataImportService;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.mongo.*;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.core.api.service.mongo.transactions.AppliedTransactionService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
class DataImportServiceTest extends RunningServerTest {

	@Inject
	DataImportService dataImportService;

	@Inject
	DatabaseExportService databaseExportService;

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
	StoredService storedService;
	@Inject
	AppliedTransactionService appliedTransactionService;
	@Inject
	ItemCheckoutService itemCheckoutService;
	@Inject
	TempFileService tempFileService;
	@Inject
	OqmDatabaseService oqmDatabaseService;

	// TODO:: fix flakiness. https://www.baeldung.com/junit-5-repeated-test
	@Test
	public void testImportService() throws IOException {
		User testUser = this.getTestUserService().getTestUser(true);
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
					.setSymbol(FAKER.food().dish().replace(" ", ""))
			);
			this.customUnitService.add(null, customUnitEntry);
		}
		List<CustomUnitEntry> customUnits = this.customUnitService.list();

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
					.setName("Custom Unit " + unitCount)
					.setSymbol("u"+unitCount)
			);
			this.customUnitService.add(null, customUnitEntry);
		}
		customUnits = this.customUnitService.list();
		log.info("Custom units ({}): {}", customUnits.size(), customUnits);

		File tempFilesDir = this.tempFileService.getTempDir("import-test-files", null);
		for (int i = 0; i < 5; i++) {
			FileAttachment attachment = new FileAttachment();
			attachment.setDescription(FAKER.lorem().paragraph());
			attachment.getAttributes().put("key", "val");
			attachment.getKeywords().add("hello world");

			File curFile = new File(tempFilesDir, i + "-" + 0 + ".txt");

			FileUtils.writeStringToFile(curFile, FAKER.lorem().paragraph(), Charset.defaultCharset());

			ObjectId id = this.fileAttachmentService.add(DEFAULT_TEST_DB_NAME, attachment, curFile, testUser).getId();

			for (int j = 1; j <= 3; j++) {
				curFile = new File(tempFilesDir, i + "-" + j + ".txt");
				FileUtils.writeStringToFile(curFile, FAKER.lorem().paragraph(), Charset.defaultCharset());
				this.fileAttachmentService.updateFile(DEFAULT_TEST_DB_NAME, id, curFile, testUser);
			}
		}
		log.info("Dbs mid adding: {}", this.oqmDatabaseService.listIterator().into(new ArrayList<>()));

		//Add images
		for (int i = 0; i < 5; i++) {
			Image curImage = new Image();
			curImage.setDescription(FAKER.lorem().paragraph());
			curImage.getAttributes().put("key", "val");
			curImage.getKeywords().add("hello world");

			File imageFile = new File(DataImportServiceTest.class.getResource("/testFiles/test_image.png").getFile());

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

			itemCategoryIds.add(this.itemCategoryService.add(DEFAULT_TEST_DB_NAME, curCategory, testUser).getId());
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
			storageIds.add(this.storageBlockService.add(DEFAULT_TEST_DB_NAME, storageBlock, testUser).getId());
		}

		for (int i = 0; i < StorageType.values().length*2; i++) {
			log.debug("StorageType: {}", StorageType.values()[i % StorageType.values().length]);
		}


		//add items
		List<ObjectId> itemIds = new ArrayList<>();
		List<InventoryItem> items = new ArrayList<>();
		for (int i = 0; i < StorageType.values().length*2; i++) {
			//TODO:: different item types
			InventoryItem item = new InventoryItem();
			item.setDescription(FAKER.lorem().paragraph());
			item.setStorageType(
				StorageType.values()[i % StorageType.values().length]
			);
			item.setName(FAKER.name().name());
			item.setUnit(customUnits.get(rand.nextInt(customUnits.size())).getUnitCreator().toUnit());

			item.getAttributes().put("key", "val");
			item.getKeywords().add("hello world");
			ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, testUser).getId();
			itemIds.add(newId);
			items.add(item);
		}
		//TODO:: stored items
		//TODO:: do some transactions

		//add item checkouts
		for (int i = 0; i < 1; i++) {
			{
				InventoryItem checkingOutItem = items.get(rand.nextInt(items.size()));

				//TODO:: rework
//				LinkedList<Map.Entry<ObjectId, SingleAmountStoredWrapper>> storedEntries = new LinkedList<>(checkingOutItem.getStorageMap().entrySet());
//
//				Map.Entry<ObjectId, SingleAmountStoredWrapper> checkingOutEntry = storedEntries.removeFirst();
//				ObjectId checkoutId = this.itemCheckoutService.checkoutItem(
//					DEFAULT_TEST_DB_NAME,
//					ItemCheckoutRequest.builder()
//						.item(checkingOutItem.getId())
//						.checkedOutFrom(checkingOutEntry.getKey())
//						.toCheckout(checkingOutEntry.getValue().getStored())
//						.checkedOutFor(new CheckoutForOqmEntity(testUser.getId()))
//						.reason(FAKER.lorem().paragraph())
//						.notes(FAKER.lorem().paragraph())
//						.build(),
//					testUser
//				);
			}
			//TODO:: rest of item types
		}
		File bundle = this.databaseExportService.exportDataToBundle(DataExportOptions.builder().build());

		FileUtils.copyFile(bundle, new File("build/export" + DatabaseExportService.OQM_EXPORT_FILE_EXT));


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
				.map((Image a) -> {
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
				.map((FileAttachment a) -> {
					return FileAttachmentGet.fromFileAttachment(a, fileAttachmentService.getRevisions(DEFAULT_TEST_DB_NAME, a.getId()));
				})
				.toList();
		this.fileAttachmentService.removeAll(DEFAULT_TEST_DB_NAME, null, testUser);
		this.fileAttachmentService.getFileObjectService().getHistoryService().removeAll(DEFAULT_TEST_DB_NAME);

//		List<CustomUnitEntry> oldUnits = this.customUnitService.list(null, Sorts.ascending("order"), null);
		List<CustomUnitEntry> oldUnits = this.customUnitService.list();
		this.customUnitService.removeAll();
		UnitUtils.reInitUnitCollections();

		log.info("Bundle to test: {}", bundle);
		log.info("Size of file bundle: {}", bundle.length());

		try (InputStream is = new FileInputStream(bundle)) {
			this.dataImportService.importBundle(is, "test"+DatabaseExportService.OQM_EXPORT_FILE_EXT, testUser, DataImportOptions.builder().build());
		}

		//TODO:: catch assertion exception, write both lists out to file

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