package tech.ebp.oqm.baseStation.service.importExport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.exception.DataExportException;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.TempFileService;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.baseStation.service.mongo.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.baseStation.service.mongo.MongoObjectService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.ObjectUtils;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.zip.Deflater;

@Traced
@Slf4j
@ApplicationScoped
public class DataExportService {
	
	private static final DateTimeFormatter FILENAME_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy_kk-mm");
	private static final String EXPORT_TEMP_DIR_PREFIX = "oqm-data-export";
	
	
	private static <T extends MainObject, S extends SearchObject<T>> void recordRecords(
		File tempDir,
		MongoObjectService<T, S> service,
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
			
			if (service instanceof MongoHistoriedObjectService && includeHistory) {
				File objectHistoryDataDir = new File(objectDataDir, DataImportExportUtils.OBJECT_HISTORY_DIR_NAME);
				
				if (!objectHistoryDataDir.mkdir()) {
					log.error("Failed to create export of data. Failed to create directory for object history.");
					throw new IOException("Failed to create directory for object history.");
				}
				
				Iterator<ObjectHistoryEvent> hIt = ((MongoHistoriedObjectService<T, S>) service).historyIterator();
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
		} catch(Throwable e){
			throw new DataExportException("Failed to export data for " + service.getClazz().getName() + ": " + e.getMessage(), e);
		}
		
		sw.stop();
		log.info("Took {} to write all data for {}", sw, dataTypeName);
	}
	
	@Inject
	TempFileService tempFileService;
	
	@Inject
	CustomUnitService customUnitService;
	
	@Inject
	FileAttachmentService fileAttachmentService;
	@Inject
	ImageService imageService;
	
	@Inject
	StorageBlockService storageBlockService;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	
	public File exportDataToBundle(boolean excludeHistory) throws IOException {
		log.info("Generating new export bundle.");
		StopWatch mainSw = StopWatch.createStarted();
		
		File dirToArchive = this.tempFileService.getTempDir("oqm_export", "export");
		Path dirToArchiveAsPath = dirToArchive.toPath();
		File outputFile = new File(dirToArchive.getParentFile(), dirToArchive.getName() + ".tar.gz");
		outputFile.deleteOnExit();
		
		log.info("Directory used to hold files: {}", dirToArchive);
		log.info("Output file: {}", outputFile);
		
		log.info("Writing service data to files.");
		{
			StopWatch sw = StopWatch.createStarted();
			
			CompletableFuture<Void> future = CompletableFuture.allOf(
				CompletableFuture.supplyAsync(()->{recordRecords(dirToArchive, this.customUnitService, !excludeHistory); return null;}),
//				CompletableFuture.supplyAsync(()->{recordRecords(dirToArchive, this.fileAttachmentService, !excludeHistory); return null;}),
				CompletableFuture.supplyAsync(()->{recordRecords(dirToArchive, this.imageService, !excludeHistory); return null;}),
				CompletableFuture.supplyAsync(()->{recordRecords(dirToArchive, this.storageBlockService, !excludeHistory); return null;}),
				CompletableFuture.supplyAsync(()->{recordRecords(dirToArchive, this.inventoryItemService, !excludeHistory); return null;})
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
			parameters.setComment("Created by Open QuarterMaster Base Station. Full data export, intended to be re-imported by the Base Station software.");
			parameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
			
			StopWatch sw = StopWatch.createStarted();
			
			try (
				OutputStream fOut = new FileOutputStream(outputFile);
				BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
				GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut, parameters);
				TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)
			) {
				Files.walkFileTree(dirToArchiveAsPath, new SimpleFileVisitor<>() {
					@Override
					public FileVisitResult visitFile(
						java.nio.file.Path file,
						BasicFileAttributes attributes
					) {
						// only copy files, no symbolic links
						if (attributes.isSymbolicLink()) {
							return FileVisitResult.CONTINUE;
						}
						
						// get filename
						java.nio.file.Path targetFile = dirToArchiveAsPath.relativize(file);
						
						try {
							TarArchiveEntry tarEntry = new TarArchiveEntry(
								file.toFile(), targetFile.toString()
							);
							tOut.putArchiveEntry(tarEntry);
							Files.copy(file, tOut);
							tOut.closeArchiveEntry();
							System.out.printf("file : %s%n", file);
						} catch(IOException e) {
							System.err.printf("Unable to tar.gz : %s%n%s%n", file, e);
						}
						return FileVisitResult.CONTINUE;
					}
					
					@Override
					public FileVisitResult visitFileFailed(java.nio.file.Path file, IOException exc) {
						System.err.printf("Unable to tar.gz : %s%n%s%n", file, exc);
						return FileVisitResult.CONTINUE;
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
