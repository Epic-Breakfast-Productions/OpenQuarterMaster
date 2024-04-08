package tech.ebp.oqm.core.api.service.importExport;

import com.mongodb.client.gridfs.model.GridFSFile;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
import tech.ebp.oqm.core.api.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.TempFileService;
import tech.ebp.oqm.core.api.service.mongo.CustomUnitService;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.ItemCategoryService;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.ItemListService;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.core.api.service.mongo.MongoObjectService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.core.api.service.mongo.file.MongoFileService;

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
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.zip.Deflater;

@Slf4j
@ApplicationScoped
public class DataExportService {
	
	public static final String OQM_EXPORT_PREFIX = "oqm_export";
	public static final String TEMP_FOLDER = "export";
	public static final String GZIP_COMMENT = "Created by Open QuarterMaster Base Station. Full data export, intended to be re-imported by the Base Station software.";
	public static final int GZIP_COMPRESSION_LEVEL = Deflater.BEST_COMPRESSION;
	private static final DateFormat fileRevisionTimestampFormat = new SimpleDateFormat("MM-dd-yyyy_hh-mm-ss-SSS");
	
	private static <T extends MainObject, S extends SearchObject<T>> void recordRecords(
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
			
			Iterator<T> it = service.iterator();
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
				
				Iterator<ObjectHistoryEvent> hIt = ((MongoHistoriedObjectService<T, S, ?>) service).historyIterator();
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
		} catch(Throwable e) {
			throw new DataExportException("Failed to export data for " + service.getClazz().getName() + ": " + e.getMessage(), e);
		}
		
		sw.stop();
		log.info("Took {} to write all data for {}", sw, dataTypeName);
	}
	
	private static <T extends FileMainObject, S extends SearchObject<T>, G extends FileGet> void recordRecords(
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
				CompletableFuture.supplyAsync(()->{
					recordRecords(
						mainDir,
						fileService.getFileObjectService(),
						includeHistory
					);
					return null;
				}),
				CompletableFuture.supplyAsync(()->{
					Iterator<GridFSFile> it = fileService.fileIterator();
					
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
							fileService.getFileContents(curGridFile.getObjectId(), os);
						} catch(IOException e) {
							log.error("FAILED to write files: ", e);
							throw new RuntimeException(e);
						}
					}
					
					return null;
				})
			);
			try {
				future.get();
			} catch(Throwable e) {
				throw new DataExportException("Failed to export service(s) data.", e);
			}
		} catch(Throwable e) {
			throw new DataExportException("Failed to export data for " + fileService.getClazz().getName() + ": " + e.getMessage(), e);
		}
		
		sw.stop();
		log.info("Took {} to write all data for {}", sw, dataTypeName);
	}
	
	
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
	ItemListService itemListService;
	
	@Inject
	ItemCheckoutService itemCheckoutService;
	
	@WithSpan
	public File exportDataToBundle(boolean excludeHistory) throws IOException {
		log.info("Generating new export bundle.");
		StopWatch mainSw = StopWatch.createStarted();
		
		File dirToArchive = this.tempFileService.getTempDir(OQM_EXPORT_PREFIX, TEMP_FOLDER);
		Path dirToArchiveAsPath = dirToArchive.toPath();
		File outputFile = new File(dirToArchive.getParentFile(), dirToArchive.getName() + ".tar.gz");
		outputFile.deleteOnExit();
		
		log.info("Directory used to hold files: {}", dirToArchive);
		log.info("Output file: {}", outputFile);
		
		log.info("Writing service data to files.");
		{
			StopWatch sw = StopWatch.createStarted();
			
			CompletableFuture<Void> future = CompletableFuture.allOf(
				CompletableFuture.supplyAsync(()->{
					recordRecords(dirToArchive, this.customUnitService, !excludeHistory);
					return null;
				}),
				CompletableFuture.supplyAsync(()->{
					recordRecords(dirToArchive, this.fileAttachmentService, !excludeHistory);
					return null;
				}),
				CompletableFuture.supplyAsync(()->{
					recordRecords(dirToArchive, this.imageService, !excludeHistory);
					return null;
				}),
				CompletableFuture.supplyAsync(()->{
					recordRecords(dirToArchive, this.itemCategoryService, !excludeHistory);
					return null;
				}),
				CompletableFuture.supplyAsync(()->{
					recordRecords(dirToArchive, this.storageBlockService, !excludeHistory);
					return null;
				}),
				CompletableFuture.supplyAsync(()->{
					recordRecords(dirToArchive, this.inventoryItemService, !excludeHistory);
					return null;
				}),
				CompletableFuture.supplyAsync(()->{
					recordRecords(dirToArchive, this.itemListService, !excludeHistory);
					return null;
				}),
				CompletableFuture.supplyAsync(()->{
					recordRecords(dirToArchive, this.itemCheckoutService, !excludeHistory);
					return null;
				})
			);
			try {
				future.get();
			} catch(Throwable e) {
				throw new DataExportException("Failed to export service(s) data.", e);
			}
			
			sw.stop();
			log.info("Took {} to generate files.", sw);
		}
		
		log.info("Compressing files into archive.");
		if (!outputFile.createNewFile()) {
			log.error("Failed to create export of data. Failed to create archive file.");
			throw new IOException("Failed to create export of data. Failed to create archive file.");
		}
		{
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
						} catch(IOException e) {
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
