package tech.ebp.oqm.core.api.service.importExport;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.Sorts;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.time.StopWatch;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.itemList.ItemList;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.media.ImageGet;
import tech.ebp.oqm.core.api.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.rest.dataImportExport.DataImportResult;
import tech.ebp.oqm.core.api.rest.dataImportExport.ImportBundleFileBody;
import tech.ebp.oqm.core.api.rest.file.FileUploadBody;
import tech.ebp.oqm.core.api.rest.search.ItemCategorySearch;
import tech.ebp.oqm.core.api.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.core.api.rest.search.ImageSearch;
import tech.ebp.oqm.core.api.rest.search.InventoryItemSearch;
import tech.ebp.oqm.core.api.rest.search.ItemCheckoutSearch;
import tech.ebp.oqm.core.api.rest.search.ItemListSearch;
import tech.ebp.oqm.core.api.rest.search.StorageBlockSearch;
import tech.ebp.oqm.core.api.service.importExport.importer.GenericFileImporter;
import tech.ebp.oqm.core.api.service.importExport.importer.GenericImporter;
import tech.ebp.oqm.core.api.service.importExport.importer.HasParentImporter;
import tech.ebp.oqm.core.api.service.importExport.importer.UnitImporter;
import tech.ebp.oqm.core.api.service.mongo.CustomUnitService;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.ItemCategoryService;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.ItemListService;
import tech.ebp.oqm.core.api.service.mongo.MongoDbAwareService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
public class DataImportService {
	
	private static final String IMPORT_TEMP_DIR_PREFIX = "oqm-data-import";
	
	private static Path zipSlipProtect(ArchiveEntry entry, Path targetDir)
		throws IOException {
		
		Path targetDirResolved = targetDir.resolve(entry.getName());
		
		// make sure normalized file still has targetDir as its prefix,
		// else throws exception
		Path normalizePath = targetDirResolved.normalize();
		
		if (!normalizePath.startsWith(targetDir)) {
			throw new IOException("Bad entry: " + entry.getName());
		}
		
		return normalizePath;
	}
	
	
	private static List<File> getObjectFiles(Path directory) throws IOException {
		try (
			Stream<Path> paths = Files.walk(
				directory,
				1
			)
		) {
			return paths
					   .filter(Files::isRegularFile)
					   .filter((Path path)->{
						   return path.toString().endsWith(".json");
					   })
					   .map(Path::toFile)
					   .collect(Collectors.toList());
		}
	}
	
	private static List<File> getObjectHistoryFiles(Path directory) throws IOException {
		Path historyDir = directory.resolve(DataImportExportUtils.OBJECT_HISTORY_DIR_NAME);
		if (historyDir.toFile().exists()) {
			return getObjectFiles(historyDir);
		}
		return List.of();
	}
	
	
	@Inject
	CustomUnitService customUnitService;
	
	@Inject
	ImageService imageService;
	@Inject
	ItemCategoryService itemItemCategoryService;
	
	@Inject
	StorageBlockService storageBlockService;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@Inject
	ItemListService itemListService;
	
	@Inject
	ItemCheckoutService itemCheckoutService;
	
	@Inject
	FileAttachmentService fileAttachmentService;
	
	private UnitImporter unitImporter;
	private GenericFileImporter<FileAttachment, FileUploadBody, FileAttachmentSearch, FileAttachmentGet> fileImporter;
	private GenericFileImporter<Image, FileUploadBody, ImageSearch, ImageGet> imageImporter;
	private HasParentImporter<ItemCategory, ItemCategorySearch> itemCategoryImporter;//TODO:: will need parent-aware importer like storage block
	private HasParentImporter<StorageBlock, StorageBlockSearch> storageBlockImporter;
	private GenericImporter<InventoryItem, InventoryItemSearch> itemImporter;
	private GenericImporter<ItemList, ItemListSearch> itemListImporter;
	private GenericImporter<ItemCheckout, ItemCheckoutSearch> itemCheckoutImporter;
	
	@PostConstruct
	public void setup(){
		this.unitImporter = new UnitImporter(this.customUnitService);
		this.itemCategoryImporter = new HasParentImporter<>(this.itemItemCategoryService);
		this.storageBlockImporter = new HasParentImporter<>(this.storageBlockService);
		this.fileImporter = new GenericFileImporter<>(this.fileAttachmentService);
		this.imageImporter = new GenericFileImporter<>(this.imageService);
		this.itemImporter = new GenericImporter<>(this.inventoryItemService);
		this.itemListImporter = new GenericImporter<>(this.itemListService);
		this.itemCheckoutImporter = new GenericImporter<>(this.itemCheckoutService);
	}
	
	@WithSpan
	public DataImportResult importBundle(
		InputStream bundleInputStream,
		String fileName,
		InteractingEntity importingEntity
	) throws IOException {
		if (!fileName.endsWith(".tar.gz")) {
			throw new IllegalArgumentException("Invalid file type given.");
		}
		
		java.nio.file.Path tempDirPath = Files.createTempDirectory(IMPORT_TEMP_DIR_PREFIX);
		File tempDir = tempDirPath.toFile();
		tempDir.deleteOnExit();
		
		StopWatch sw = StopWatch.createStarted();
		log.info("Decompressing given bundle.");
		try (
			BufferedInputStream bi = new BufferedInputStream(bundleInputStream);
			GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
			TarArchiveInputStream ti = new TarArchiveInputStream(gzi)
		) {
			
			ArchiveEntry entry;
			while ((entry = ti.getNextEntry()) != null) {
				
				// create a new path, zip slip validate
				Path newPath = zipSlipProtect(entry, tempDirPath);
				
				if (entry.isDirectory()) {
					Files.createDirectories(newPath);
				} else {
					
					// check parent folder again
					Path parent = newPath.getParent();
					if (parent != null) {
						if (Files.notExists(parent)) {
							Files.createDirectories(parent);
						}
					}
					
					// copy TarArchiveInputStream to Path newPath
					Files.copy(ti, newPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
		sw.stop();
		log.info("Finished decompressing bundle, took {}", sw);
		
		// TODO:: validate data to read in. Ensure no errors will happen when adding to database. No optimal way to do this?
		
		log.info("Reading in objects.");
		sw = StopWatch.createStarted();
		DataImportResult.Builder<?, ?> resultBuilder = DataImportResult.builder();
		
		try(
			ClientSession session = this.imageService.getNewClientSession();//shouldn't matter which mongo service to grab session from
		){
			session.withTransaction(()->{
				try {
					resultBuilder.numUnits(this.unitImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numFileAttachments(this.fileImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numImages(this.imageImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numItemCategories(this.itemCategoryImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numStorageBlocks(this.storageBlockImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numInventoryItems(this.itemImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numItemLists(this.itemListImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numItemLists(this.itemCheckoutImporter.readInObjects(session, tempDirPath, importingEntity));
					//TODO:: history
				} catch(Throwable e){
					session.abortTransaction();
					throw new RuntimeException("A data error prevented import of the bundle: " + e.getMessage(), e);
				}
				session.commitTransaction();
				return true;
			}, MongoDbAwareService.getDefaultTransactionOptions());
		} catch(Throwable e){
			UnitUtils.reInitUnitCollections();
			
			this.customUnitService.listIterator(
				null,
				Sorts.ascending("order"),
				null
			).forEach(UnitUtils::registerAllUnits);
			
			throw e;
		}
		
		sw.stop();
		log.info("Finished reading in objects. Took {}", sw);
		
		return resultBuilder.build();
	}
	
	@WithSpan
	public DataImportResult importBundle(
		ImportBundleFileBody body,
		InteractingEntity importingEntity
	) throws IOException {
		return this.importBundle(body.file, body.fileName, importingEntity);
	}
	
}
