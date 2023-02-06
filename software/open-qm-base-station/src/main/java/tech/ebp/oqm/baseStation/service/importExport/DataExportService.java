package tech.ebp.oqm.baseStation.service.importExport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.baseStation.service.mongo.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.baseStation.service.mongo.MongoObjectService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
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
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

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
	) throws IOException {
		String dataTypeName = service.getCollectionName();
		log.info("Writing {} data to archive folder.", dataTypeName);
		StopWatch sw = StopWatch.createStarted();
		File objectDataDir = new File(tempDir, dataTypeName);
		
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
		
		sw.stop();
		log.info("Took {} to write all data for {}", sw, dataTypeName);
	}
	
	@Inject
	CustomUnitService customUnitService;
	
	@Inject
	ImageService imageService;
	
	@Inject
	StorageBlockService storageBlockService;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	
	
	public File exportDataToBundle(boolean excludeHistory) throws IOException {
		log.info("Generating new export bundle.");
		StopWatch mainSw = StopWatch.createStarted();
		
		//create temp folder to do all work in
		java.nio.file.Path tempDirPath = Files.createTempDirectory(EXPORT_TEMP_DIR_PREFIX);
		
		File tempDir = tempDirPath.toFile();
		tempDir.deleteOnExit();
		String exportFileName = "oqm_export_" + ZonedDateTime.now().format(FILENAME_TIMESTAMP_FORMAT) + ".tar.gz";
		File outputFile = new File(tempDir, exportFileName);
		File dirToArchive = new File(tempDir, "oqm-export");
		java.nio.file.Path dirToArchiveAsPath = dirToArchive.toPath();
		
		log.info("Temp dir: {}", tempDir);
		log.info("Output file: {}", outputFile);
		
		if (!dirToArchive.mkdir()) {
			log.error("Failed to create export of data. Failed to create directory.");
			throw new IOException("Failed to create export of data. Failed to create directory.");
		}
		
		log.info("Writing service data to files.");
		{
			StopWatch sw = StopWatch.createStarted();
			//TODO:: parallelize
			recordRecords(dirToArchive, this.customUnitService, !excludeHistory);
			recordRecords(dirToArchive, this.imageService, !excludeHistory);
			recordRecords(dirToArchive, this.storageBlockService, !excludeHistory);
			recordRecords(dirToArchive, this.inventoryItemService, !excludeHistory);
			sw.stop();
			log.info("Took {} to generate files.", sw);
		}
		
		log.info("Compressing files into archive.");
		if (!outputFile.createNewFile()) {
			log.error("Failed to create export of data. Failed to create archive file.");
			throw new IOException("Failed to create export of data. Failed to create archive file.");
		}
		{
			StopWatch sw = StopWatch.createStarted();
			
			try (
				OutputStream fOut = new FileOutputStream(outputFile);
				BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
				GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);
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
