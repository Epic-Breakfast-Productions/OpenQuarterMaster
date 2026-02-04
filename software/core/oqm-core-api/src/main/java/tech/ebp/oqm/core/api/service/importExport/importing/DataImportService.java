package tech.ebp.oqm.core.api.service.importExport.importing;

import com.mongodb.client.ClientSession;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.exception.DataImportException;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.itemList.ItemList;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.media.ImageGet;
import tech.ebp.oqm.core.api.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.core.api.model.rest.dataImportExport.DataImportResult;
import tech.ebp.oqm.core.api.model.rest.dataImportExport.DbImportResult;
import tech.ebp.oqm.core.api.model.rest.dataImportExport.EntityImportResult;
import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.model.rest.search.*;
import tech.ebp.oqm.core.api.service.importExport.exporting.DataImportExportUtils;
import tech.ebp.oqm.core.api.service.importExport.exporting.DatabaseExportService;
import tech.ebp.oqm.core.api.service.importExport.importing.importer.*;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.mongo.*;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.core.api.service.mongo.transactions.AppliedTransactionService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tech.ebp.oqm.core.api.service.importExport.ImportExportConstants.*;

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
				.filter((Path path) -> {
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
	
	private static File decompressBundle(String fileName, InputStream bundleInputStream) throws IOException {
		if (!fileName.endsWith(DatabaseExportService.OQM_EXPORT_FILE_EXT)) {
			throw new IllegalArgumentException("Invalid file type given.");
		}
		
		Path tempArchiveDirPath = Files.createTempDirectory(IMPORT_TEMP_DIR_PREFIX);
		File tempArchiveDir = tempArchiveDirPath.toFile();
		tempArchiveDir.deleteOnExit();
		
		StopWatch sw = StopWatch.createStarted();
		long numFiles = 0;
		log.info("Decompressing given bundle.");
		try (
			BufferedInputStream bi = new BufferedInputStream(bundleInputStream);
			GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
			TarArchiveInputStream ti = new TarArchiveInputStream(gzi)
		) {
			ArchiveEntry entry;
			while ((entry = ti.getNextEntry()) != null) {
				// create a new path, zip slip validate
				Path newPath = zipSlipProtect(entry, tempArchiveDirPath);
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
					log.debug("Decompressed file {}", newPath);
					numFiles++;
				}
			}
		}
		sw.stop();
		log.info("Finished decompressing bundle, numFiles: {}, took {}", numFiles, sw);
		return tempArchiveDir;
	}

	@Inject
	OqmDatabaseService oqmDatabaseService;

	@Inject
	ImageService imageService;

	@Inject
	ItemCategoryService itemItemCategoryService;

	@Inject
	StorageBlockService storageBlockService;

	@Inject
	InventoryItemService inventoryItemService;

	@Inject
	StoredService storedService;

	@Inject
	AppliedTransactionService appliedTransactionService;

	@Inject
	ItemListService itemListService;

	@Inject
	ItemCheckoutService itemCheckoutService;

	@Inject
	FileAttachmentService fileAttachmentService;

	@Inject
	UnitImporter unitImporter;
	@Inject
	InteractingEntityImporter interactingEntityImporter;

	private GenericFileImporter<FileAttachment, FileUploadBody, FileAttachmentSearch, FileAttachmentGet> fileImporter;
	private GenericFileImporter<Image, FileUploadBody, ImageSearch, ImageGet> imageImporter;
	private HasParentImporterHistoried<ItemCategory, ItemCategorySearch> itemCategoryImporter;
	private HasParentImporterHistoried<StorageBlock, StorageBlockSearch> storageBlockImporter;
	private GenericImporterHistoried<InventoryItem, InventoryItemSearch> itemImporter;
	private GenericImporterHistoried<Stored, StoredSearch> storedImporter;
	private GenericImporter<AppliedTransaction, AppliedTransactionSearch> appliedTransactionImporter;
	private GenericImporterHistoried<ItemList, ItemListSearch> itemListImporter;
	private GenericImporterHistoried<ItemCheckout, ItemCheckoutSearch> itemCheckoutImporter;

	@PostConstruct
	public void setup() {
		this.itemCategoryImporter = new HasParentImporterHistoried<>(this.itemItemCategoryService);
		this.storageBlockImporter = new HasParentImporterHistoried<>(this.storageBlockService);
		this.fileImporter = new GenericFileImporter<>(this.fileAttachmentService);
		this.imageImporter = new GenericFileImporter<>(this.imageService);
		this.itemImporter = new GenericImporterHistoried<>(this.inventoryItemService);
		this.storedImporter = new GenericImporterHistoried<>(this.storedService);
		this.appliedTransactionImporter = new GenericImporter<>(this.appliedTransactionService);
		this.itemListImporter = new GenericImporterHistoried<>(this.itemListService);
		this.itemCheckoutImporter = new GenericImporterHistoried<>(this.itemCheckoutService);
	}

	public DataImportResult importBundle(
		InputStream bundleInputStream,
		String fileName,
		InteractingEntity importingEntity,
		DataImportOptions importOptions
	) throws IOException {
		log.info("Importing bundle {}", fileName);
		File tempArchiveDir = decompressBundle(fileName, bundleInputStream);

		File topLevelDir = new File(tempArchiveDir, TOP_LEVEL_DIR_NAME);
		Path topLevelDirPath = topLevelDir.toPath();
		File dbsDir = new File(tempArchiveDir, DBS_DIR_NAME);
		Path dbsDirPath = dbsDir.toPath();


		Set<File> dbDirs = Arrays.asList(dbsDir.listFiles()).stream()
			.filter(File::isDirectory)
			.collect(Collectors.toSet());

		log.info("Database directories found: {}", dbDirs);
		
		// check dbs
		//TODO: gracefully handle case of no dbs in bundle
		List<OqmMongoDatabase> databasesToImport = dbDirs.stream()
			.map((File dbDir) -> {
				File dbInfoFile = new File(dbDir, DB_INFO_FILE_NAME);
				try {
					return ObjectUtils.OBJECT_MAPPER.readValue(dbInfoFile, OqmMongoDatabase.class);
				} catch (IOException e) {
					throw new DataImportException("Failed to deserialize db info: " + dbInfoFile, e);
				}
			})
			.filter(db -> importOptions.getDatabaseSelection().isSelected(db))
			.filter(db -> {
				boolean alreadyExistent = oqmDatabaseService.hasDatabase(db);
				return switch (importOptions.getDbMergeStrategy()) {
					case MERGE, RENAME -> true;
					case SKIP -> !alreadyExistent;
					case ERROR -> {
						if (alreadyExistent) {
							throw new DataImportException("Database already exists: " + db.toString());
						}
						yield true;
					}
				};
			})
			.toList();

		log.info("Starting the reading in of object data.");
		StopWatch sw = StopWatch.createStarted();
		DataImportResult.DataImportResultBuilder<?, ?> resultBuilder = DataImportResult.builder();

		try (
			ClientSession session = this.imageService.getNewClientSession(true);//shouldn't matter which mongo service to grab session from
		) {
			try {
				Map<ObjectId, ObjectId> entityIdMap;
				{// import top level data
					log.info("Reading in top level objects.");
					StopWatch topLevelStopwatch = StopWatch.createStarted();
					EntityImportResult entityImportResult = this.interactingEntityImporter.readInObjects(session, topLevelDirPath, importingEntity, importOptions);
					entityIdMap = entityImportResult.getInteractingEntitiesMapped();
					resultBuilder.entities(entityImportResult);
					resultBuilder.numUnits(this.unitImporter.readInObjects(session, topLevelDirPath, importingEntity, importOptions));
					topLevelStopwatch.stop();
					log.info("Done reading in top level objects. Took {}", topLevelStopwatch);
				}
				
				
				Map<String, DbImportResult> dbImportResults = new HashMap<>();
				// import db data
				for (OqmMongoDatabase curDb : databasesToImport) {
					log.info("Importing database {}", curDb);
					boolean dbAlreadyExistent = oqmDatabaseService.hasDatabase(curDb);
					if (dbAlreadyExistent) {
						//determine how to deal with this
						switch (importOptions.getDbMergeStrategy()) {
							case MERGE -> {
								curDb = this.oqmDatabaseService.getOqmDatabase(curDb.getName()).getOqmMongoDatabase();
							}
							case RENAME -> {
								curDb.setName(curDb.getName() + "-" + RandomStringUtils.randomAlphanumeric(3));
								this.oqmDatabaseService.addOqmDatabase(curDb);
							}
							case SKIP, ERROR -> {
								throw new IllegalStateException("We should not be able to get here");
							}
						}
					} else {//is a new database
						this.oqmDatabaseService.addOqmDatabase(curDb);
					}

					Path curDbPath = dbsDirPath.resolve(curDb.getName());
					OqmMongoDatabase finalCurDb = curDb;//cause dumb
					
					DbImportResult.DbImportResultBuilder dbResultBuilder = DbImportResult.builder();
					
					try {
						dbResultBuilder.numFileAttachments(this.fileImporter.readInObjects(finalCurDb.getId(), session, curDbPath, importingEntity, importOptions, entityIdMap));
						dbResultBuilder.numImages(this.imageImporter.readInObjects(finalCurDb.getId(), session, curDbPath, importingEntity, importOptions, entityIdMap));
						dbResultBuilder.numItemCategories(this.itemCategoryImporter.readInObjects(finalCurDb.getId(), session, curDbPath, importingEntity, importOptions, entityIdMap));
						dbResultBuilder.numStorageBlocks(this.storageBlockImporter.readInObjects(finalCurDb.getId(), session, curDbPath, importingEntity, importOptions, entityIdMap));
						dbResultBuilder.numInventoryItems(this.itemImporter.readInObjects(finalCurDb.getId(), session, curDbPath, importingEntity, importOptions, entityIdMap));
						dbResultBuilder.numStored(this.storedImporter.readInObjects(finalCurDb.getId(), session, curDbPath, importingEntity, importOptions, entityIdMap));
						dbResultBuilder.numTransactions(this.appliedTransactionImporter.readInObjects(finalCurDb.getId(), session, curDbPath, importingEntity, importOptions, entityIdMap));
						dbResultBuilder.numItemLists(this.itemListImporter.readInObjects(finalCurDb.getId(), session, curDbPath, importingEntity, importOptions, entityIdMap));
						dbResultBuilder.numItemCheckouts(this.itemCheckoutImporter.readInObjects(finalCurDb.getId(), session, curDbPath, importingEntity, importOptions, entityIdMap));
					} catch (IOException e) {
						throw new DataImportException("Failed to read in database " + finalCurDb.getName(), e);
					}
					
					dbImportResults.put(curDb.getName(), dbResultBuilder.build());
				}
				resultBuilder.dbResults(dbImportResults);

			} catch (Throwable e) {
				session.abortTransaction();
				throw new RuntimeException("A data error prevented import of the bundle: " + e.getMessage(), e);
			}
			session.commitTransaction();
		}

		return resultBuilder.build();
	}
}
