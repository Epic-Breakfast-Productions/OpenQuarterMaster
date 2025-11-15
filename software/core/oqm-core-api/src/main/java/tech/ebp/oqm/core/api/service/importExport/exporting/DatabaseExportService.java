package tech.ebp.oqm.core.api.service.importExport.exporting;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.model.GridFSFile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.exception.DataExportException;
import tech.ebp.oqm.core.api.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.core.api.model.object.FileMainObject;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.TempFileService;
import tech.ebp.oqm.core.api.service.mongo.*;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.core.api.service.mongo.file.MongoFileService;
import tech.ebp.oqm.core.api.service.mongo.transactions.AppliedTransactionService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.Deflater;

import static tech.ebp.oqm.core.api.service.importExport.ImportExportConstants.*;

@Slf4j
@ApplicationScoped
public class DatabaseExportService {

	public static final String OQM_EXPORT_PREFIX = "oqm_export";
	public static final String OQM_EXPORT_FILE_EXT = ".oqmdb";
	public static final String TEMP_FOLDER = "export";
	public static final String GZIP_COMMENT = "Created by Open QuarterMaster Base Station. Full data export, intended to be re-imported by the Base Station software.";
	public static final int GZIP_COMPRESSION_LEVEL = Deflater.BEST_COMPRESSION;
	private static final DateFormat fileRevisionTimestampFormat = new SimpleDateFormat("MM-dd-yyyy_hh-mm-ss-SSS");

	private static <T extends MainObject, S extends SearchObject<T>> void recordRecords(
		String oqmDbIdOrName,
		File tempDir,
		MongoObjectService<T, S, ?> service,
		boolean includeHistory
	) {
		String dataTypeName = service.getCollectionName();
		log.info("Writing {} data to archive folder.", dataTypeName);

		StopWatch sw = StopWatch.createStarted();
		File objectDataDir = new File(tempDir, dataTypeName);

		try {

			if (!objectDataDir.mkdir()) {
				log.error("Failed to create export of data. Failed to create directory.");
				throw new IOException("Failed to create directory.");
			}

			Iterator<T> it = service.iterator(oqmDbIdOrName);
			while (it.hasNext()) {
				T curObj = it.next();
				ObjectId curId = curObj.getId();
				File curObjectFile = new File(objectDataDir, curId.toHexString() + ".json");

				if (!curObjectFile.createNewFile()) {
					log.error("Failed to create data file for object.");
					throw new IOException("Failed to create data file for object.");
				}

				ObjectUtils.OBJECT_MAPPER.writeValue(curObjectFile, curObj);
			}

			//TODO:: refactor
			if (service instanceof MongoHistoriedObjectService && includeHistory) {
				File objectHistoryDataDir = new File(objectDataDir, DataImportExportUtils.OBJECT_HISTORY_DIR_NAME);

				if (!objectHistoryDataDir.mkdir()) {
					log.error("Failed to create export of data. Failed to create directory for object history.");
					throw new IOException("Failed to create directory for object history.");
				}

				Iterator<ObjectHistoryEvent> hIt = ((MongoHistoriedObjectService<T, S, ?>) service).historyIterator(oqmDbIdOrName);
				while (hIt.hasNext()) {
					ObjectHistoryEvent curObj = hIt.next();
					ObjectId curId = curObj.getId();
					File curObjectFile = new File(objectHistoryDataDir, curId.toHexString() + ".json");

					if (!curObjectFile.createNewFile()) {
						log.error("Failed to create data file for object history.");
						throw new IOException("Failed to create data file for object history.");
					}

					ObjectUtils.OBJECT_MAPPER.writeValue(curObjectFile, curObj);
				}
			}
		} catch (Throwable e) {
			throw new DataExportException("Failed to export data for " + service.getClazz().getName() + ": " + e.getMessage(), e);
		}

		sw.stop();
		log.info("Took {} to write all data for {}", sw, dataTypeName);
	}

	private static <T extends FileMainObject, S extends SearchObject<T>, G extends MainObject & FileGet> void recordRecords(
		String oqmDbIdOrName,
		File tempDir,
		MongoFileService<T, S, ?, G> fileService,
		boolean includeHistory
	) {
		String dataTypeName = fileService.getCollectionName();
		log.info("Writing {} data to archive folder.", dataTypeName);

		StopWatch sw = StopWatch.createStarted();
		File mainDir = new File(tempDir, dataTypeName);
		File fileDataDir = new File(mainDir, "files");

		try {
			if (!mainDir.mkdir()) {
				log.error("Failed to create export of data. Failed to create directory: {}", mainDir);
				throw new IOException("Failed to create directory for file collection " + fileService.getCollectionName());
			}
			if (!fileDataDir.mkdir()) {
				log.error("Failed to create export of data. Failed to create directory: {}", fileDataDir);
				throw new IOException("Failed to create directory for collection " + fileService.getCollectionName());
			}

			CompletableFuture<Void> future = CompletableFuture.allOf(
				CompletableFuture.supplyAsync(() -> {
					recordRecords(
						oqmDbIdOrName,
						mainDir,
						fileService.getFileObjectService(),
						includeHistory
					);
					return null;
				}),
				CompletableFuture.supplyAsync(() -> {
					Iterator<GridFSFile> it = fileService.fileIterator(oqmDbIdOrName);

					while (it.hasNext()) {
						GridFSFile curGridFile = it.next();
						FileMetadata metadata = FileMetadata.fromDocument(curGridFile.getMetadata(), fileService.getFileMetadataCodec());
						String curFileName = curGridFile.getFilename();
						String curRevisionName = fileRevisionTimestampFormat.format(curGridFile.getUploadDate());
						File curFileDir = new File(fileDataDir, curFileName);
						File curFileRevisionDir = new File(curFileDir, curRevisionName);
						File curRevisionFile = new File(curFileRevisionDir, "file." + metadata.getFileExtension());
						File curRevisionMetadata = new File(curFileRevisionDir, "metadata.json");

						curFileDir.mkdir();
						curFileRevisionDir.mkdir();

						try (
							FileOutputStream os = new FileOutputStream(curRevisionFile);
						) {
							ObjectUtils.OBJECT_MAPPER.writeValue(curRevisionMetadata, metadata);
							fileService.getFileContents(oqmDbIdOrName, curGridFile.getObjectId(), os);
						} catch (IOException e) {
							log.error("FAILED to write files: ", e);
							throw new RuntimeException(e);
						}
					}

					return null;
				})
			);
			try {
				future.get();
			} catch (Throwable e) {
				throw new DataExportException("Failed to export service(s) data.", e);
			}
		} catch (Throwable e) {
			throw new DataExportException("Failed to export data for " + fileService.getClazz().getName() + ": " + e.getMessage(), e);
		}

		sw.stop();
		log.info("Took {} to write all data for {}", sw, dataTypeName);
	}

	private static <T extends MainObject, S extends SearchObject<T>> void recordRecords(
		File tempDir,
		TopLevelMongoService<T, S, ?> service
	) {
		String dataTypeName = service.getCollectionName();
		log.info("Writing {} data to archive folder.", dataTypeName);

		StopWatch sw = StopWatch.createStarted();
		File objectDataDir = new File(tempDir, dataTypeName);

		try {
			if (!objectDataDir.mkdir()) {
				log.error("Failed to create export of data. Failed to create directory.");
				throw new IOException("Failed to create directory.");
			}

			try (MongoCursor<T> it = service.listIterator().iterator()) {
				while (it.hasNext()) {
					T curObj = it.next();
					ObjectId curId = curObj.getId();
					File curObjectFile = new File(objectDataDir, curId.toHexString() + ".json");

					if (!curObjectFile.createNewFile()) {
						log.error("Failed to create data file for object.");
						throw new IOException("Failed to create data file for object.");
					}

					ObjectUtils.OBJECT_MAPPER.writeValue(curObjectFile, curObj);
				}
			}
		} catch (Throwable e) {
			throw new DataExportException("Failed to export data for " + service.getClazz().getName() + ": " + e.getMessage(), e);
		}

		sw.stop();
		log.info("Took {} to write all data for {}", sw, dataTypeName);
	}

	@Inject
	OqmDatabaseService oqmDatabaseService;

	@Inject
	InteractingEntityService interactingEntityService;

	@Inject
	TempFileService tempFileService;

	@Inject
	CustomUnitService customUnitService;

	@Inject
	ItemCategoryService itemCategoryService;

	@Inject
	FileAttachmentService fileAttachmentService;

	@Inject
	ImageService imageService;

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

	public File exportDataToBundle(DataExportOptions options) throws IOException {
		log.info("Generating new export bundle. Options: {}", options);
		StopWatch mainSw = StopWatch.createStarted();

		File dirToArchive = this.tempFileService.getTempDir(OQM_EXPORT_PREFIX, TEMP_FOLDER);
		Path dirToArchiveAsPath = dirToArchive.toPath();
		File outputFile = new File(dirToArchive.getParentFile(), dirToArchive.getName() + OQM_EXPORT_FILE_EXT);
		outputFile.deleteOnExit();
		File topLevelDataDir = new File(dirToArchive, TOP_LEVEL_DIR_NAME);
		File dbDataDir = new File(dirToArchive, DBS_DIR_NAME);

		if (!topLevelDataDir.mkdir() || !dbDataDir.mkdir()) {
			log.error("Failed to create directories for top level and dbs.");
			throw new IOException("Failed to create directory.");
		}

		log.info("Directory used to hold files: {}", dirToArchive);
		log.info("Output file: {}", outputFile);

		{//writing out data files
			StopWatch dataToFileSw = StopWatch.createStarted();
			log.info("Writing out top level data.");
			CompletableFuture<Void> topLevelFutures = CompletableFuture.allOf(
				CompletableFuture.supplyAsync(() -> {
					recordRecords(topLevelDataDir, this.customUnitService);
					return null;
				}),
				CompletableFuture.supplyAsync(() -> {
					recordRecords(topLevelDataDir, this.interactingEntityService);
					return null;
				})
			);

			log.info("Writing out selected databases");
			Map<OqmMongoDatabase, CompletableFuture<Void>> databaseFutures = new HashMap<>();
			{
				List<OqmMongoDatabase> dbList = this.oqmDatabaseService.listIterator().into(new ArrayList<>());

				log.info("Databases available: {}", dbList);
				dbList = dbList.stream().filter(db -> options.getDatabaseSelection().isSelected(db)).toList();
				log.info("Databases to save to export bundle: {}", dbList);

				for (OqmMongoDatabase db : dbList) {
					File thisDbDir = new File(dbDataDir, db.getName());
					if (!thisDbDir.mkdir()) {
						log.error("Failed to create directory for db " + db.getName());
						throw new IOException("Failed to create directory.");
					}
					ObjectUtils.OBJECT_MAPPER.writeValue(new File(thisDbDir, DB_INFO_FILE_NAME), db);

					databaseFutures.put(db, CompletableFuture.supplyAsync(() -> {
							String dbId = db.getId().toHexString();
							StopWatch sw = StopWatch.createStarted();

							CompletableFuture<Void> future = CompletableFuture.allOf(
								CompletableFuture.supplyAsync(() -> {
									recordRecords(dbId, thisDbDir, this.fileAttachmentService, options.getIncludeHistory());
									return null;
								}),
								CompletableFuture.supplyAsync(() -> {
									recordRecords(dbId, thisDbDir, this.imageService, options.getIncludeHistory());
									return null;
								}),
								CompletableFuture.supplyAsync(() -> {
									recordRecords(dbId, thisDbDir, this.itemCategoryService, options.getIncludeHistory());
									return null;
								}),
								CompletableFuture.supplyAsync(() -> {
									recordRecords(dbId, thisDbDir, this.storageBlockService, options.getIncludeHistory());
									return null;
								}),
								CompletableFuture.supplyAsync(() -> {
									recordRecords(dbId, thisDbDir, this.inventoryItemService, options.getIncludeHistory());
									return null;
								}),
								CompletableFuture.supplyAsync(() -> {
									recordRecords(dbId, thisDbDir, this.storedService, options.getIncludeHistory());
									return null;
								}),
								CompletableFuture.supplyAsync(() -> {
									recordRecords(dbId, thisDbDir, this.appliedTransactionService, options.getIncludeHistory());
									return null;
								}),
								CompletableFuture.supplyAsync(() -> {
									recordRecords(dbId, thisDbDir, this.itemListService, options.getIncludeHistory());
									return null;
								}),
								CompletableFuture.supplyAsync(() -> {
									recordRecords(dbId, thisDbDir, this.itemCheckoutService, options.getIncludeHistory());
									return null;
								})
							);
							try {
								future.get();
							} catch (Throwable e) {
								throw new DataExportException("Failed to export service(s) data.", e);
							}

							sw.stop();
							log.info("Took {} to generate files.", sw);
							return (Void) null;
						})
					);
				}
			}

			try {
				topLevelFutures.get();
			} catch (Throwable e) {
				throw new DataExportException("Failed to export top level service(s) data.", e);
			}
			try {
				databaseFutures.forEach((OqmMongoDatabase db, CompletableFuture<Void> future) -> {
					try {
						future.get();
					} catch (Throwable e) {
						throw new DataExportException("Failed to export database \"" + db.getName() + "\" service(s) data.", e);
					}
				});
			} catch (Throwable e) {
				throw new DataExportException("Failed to export top level service(s) data.", e);
			}
			dataToFileSw.stop();
			log.info("Took {} to write out files.", dataToFileSw);
		}

		log.info("Compressing files into archive.");
		if (!outputFile.createNewFile()) {
			log.error("Failed to create export of data. Failed to create archive file.");
			throw new IOException("Failed to create export of data. Failed to initially create archive file.");
		}
		{
			//TODO:: experiment with using XZ
			GzipParameters parameters = new GzipParameters();
			parameters.setComment(GZIP_COMMENT);
			parameters.setCompressionLevel(GZIP_COMPRESSION_LEVEL);

			StopWatch sw = StopWatch.createStarted();

			try (
				OutputStream fOut = new FileOutputStream(outputFile);
				BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
				GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut, parameters);
				TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)
			) {
				tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
				Files.walkFileTree(dirToArchiveAsPath, new SimpleFileVisitor<>() {
					@Override
					public FileVisitResult visitFile(
						Path file,
						BasicFileAttributes attributes
					) {
						// only copy files, no symbolic links
						if (attributes.isSymbolicLink()) {
							return FileVisitResult.CONTINUE;
						}
						// get filename
						Path targetFile = dirToArchiveAsPath.relativize(file);

						try {
							TarArchiveEntry tarEntry = new TarArchiveEntry(
								file.toFile(), targetFile.toString()
							);
							tOut.putArchiveEntry(tarEntry);
							Files.copy(file, tOut);
							tOut.closeArchiveEntry();
							log.trace("File added to export bundle: {}", file);
						} catch (IOException e) {
							log.error("Unable to process file: {}", file, e);
							throw new DataExportException("Unable to process file: " + file + " - " + e.getMessage(), e);
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) {
						log.error("Unable to process file: {}", file, exc);
						return FileVisitResult.TERMINATE;
					}
				});
				tOut.finish();
			}
			sw.stop();
			log.info("Took {} to compress files.", sw);
		}

		mainSw.stop();
		log.info("Took {} total to generate output bundle.", mainSw);
		return outputFile;
	}
}
