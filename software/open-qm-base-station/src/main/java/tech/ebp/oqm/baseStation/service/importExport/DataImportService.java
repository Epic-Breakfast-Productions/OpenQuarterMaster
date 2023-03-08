package tech.ebp.oqm.baseStation.service.importExport;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.Sorts;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.time.StopWatch;
import tech.ebp.oqm.baseStation.rest.dataImportExport.DataImportResult;
import tech.ebp.oqm.baseStation.rest.dataImportExport.ImportBundleFileBody;
import tech.ebp.oqm.baseStation.rest.search.CategoriesSearch;
import tech.ebp.oqm.baseStation.rest.search.ImageSearch;
import tech.ebp.oqm.baseStation.rest.search.InventoryItemSearch;
import tech.ebp.oqm.baseStation.service.importExport.importer.GenericImporter;
import tech.ebp.oqm.baseStation.service.importExport.importer.StorageBlockImporter;
import tech.ebp.oqm.baseStation.service.importExport.importer.UnitImporter;
import tech.ebp.oqm.baseStation.service.mongo.CategoriesService;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.baseStation.service.mongo.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.MongoService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.media.Image;
import tech.ebp.oqm.lib.core.object.storage.ItemCategory;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.units.UnitUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
	StorageBlockService storageBlockService;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	private UnitImporter unitImporter;
	private GenericImporter<Image, ImageSearch> imageImporter;
	private GenericImporter<ItemCategory, CategoriesSearch> itemCategoryImporter;//TODO:: will need parent-aware importer like storage block
	private StorageBlockImporter storageBlockImporter;
	private GenericImporter<InventoryItem, InventoryItemSearch> itemImporter;
	
	@PostConstruct
	public void setup(){
		this.unitImporter = new UnitImporter(this.customUnitService);
		this.storageBlockImporter = new StorageBlockImporter(this.storageBlockService);
		this.imageImporter = new GenericImporter<>(this.imageService);
		this.itemImporter = new GenericImporter<>(this.inventoryItemService);
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
		DataImportResult.DataImportResultBuilder<?, ?> resultBuilder = DataImportResult.builder();
		
		try(
			ClientSession session = this.imageService.getNewClientSession();//shouldn't matter which mongo service to grab session from
		){
			session.withTransaction(()->{
				try {
					resultBuilder.numUnits(this.unitImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numImages(this.imageImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numCategories(this.itemCategoryImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numStorageBlocks(this.storageBlockImporter.readInObjects(session, tempDirPath, importingEntity));
					resultBuilder.numInventoryItems(this.itemImporter.readInObjects(session, tempDirPath, importingEntity));
					//TODO:: history
				} catch(Throwable e){
					session.abortTransaction();
					throw new RuntimeException("A data error prevented import of the bundle: " + e.getMessage(), e);
				}
				session.commitTransaction();
				return true;
			}, MongoService.getDefaultTransactionOptions());
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
