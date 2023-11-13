package tech.ebp.oqm.baseStation.service.printouts;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.ListAmountItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.TrackedItem;
import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.baseStation.rest.printouts.InventorySheetsOptions;
import tech.ebp.oqm.baseStation.rest.printouts.PageOrientation;
import tech.ebp.oqm.baseStation.rest.search.StorageBlockSearch;
import tech.ebp.oqm.baseStation.service.mongo.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

@Slf4j
@ApplicationScoped
public class StorageBlockInventorySheetService extends PrintoutDataService {
	
	private static final DateTimeFormatter FILENAME_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy_kk-mm");
	
	private static final String EXPORT_TEMP_DIR_PREFIX = "oqm-sheets";
	
	private static final ConverterProperties CONVERTER_PROPERTIES;
	
	static {
		CONVERTER_PROPERTIES = new ConverterProperties()
								   .setBaseUri(ConfigProvider.getConfig().getValue("runningInfo.baseUrl", String.class));
	}
	
	
	@Inject
	StorageBlockService storageBlockService;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@Inject
	ImageService imageService;
	
	@Inject
	@Location("printouts/storageBlockInvSheet/storageBlockInventorySheet.html")
	Template inventorySheetTemplate;
	
	private File getTempPdfFile(String name) throws IOException {
		java.nio.file.Path tempDirPath = Files.createTempDirectory(EXPORT_TEMP_DIR_PREFIX);
		File tempDir = tempDirPath.toFile();
		tempDir.deleteOnExit();
		String exportFileName =
			"oqm_storage_sheet_" + name + "_" + ZonedDateTime.now().format(FILENAME_TIMESTAMP_FORMAT) + ".pdf";
		return new File(tempDir, exportFileName);
	}
	
	private TemplateInstance getHtmlInventorySheet(
		StorageBlock storageBlock,
		StorageBlockSearch storageBlockSearch,
		List<StorageBlock> children,
		SearchResult<InventoryItem> itemsInBlock,
		InventorySheetsOptions options
	) {
		Predicate<InventoryItem> simpleAmountFilter = new Predicate<InventoryItem>() {
			@Override
			public boolean test(InventoryItem inventoryItem) {
				return inventoryItem instanceof SimpleAmountItem;
			}
		};
		Predicate<InventoryItem> listAmountFilter = new Predicate<InventoryItem>() {
			@Override
			public boolean test(InventoryItem inventoryItem) {
				return inventoryItem instanceof ListAmountItem;
			}
		};
		Predicate<InventoryItem> trackedFilter = new Predicate<InventoryItem>() {
			@Override
			public boolean test(InventoryItem inventoryItem) {
				return inventoryItem instanceof TrackedItem;
			}
		};
		
		return this.setupBasicPrintoutData(this.inventorySheetTemplate)
				   .data("simpleAmountFilter", simpleAmountFilter)
				   .data("listAmountFilter", listAmountFilter)
				   .data("trackedFilter", trackedFilter)
				   .data("options", options)
				   .data("storageBlock", storageBlock)
				   .data("storageBlockList", children)
				   .data("searchResult", itemsInBlock)
				   .data("imageService", this.imageService);
	}
	
	/**
	 * https://kb.itextpdf.com/home/it7kb/ebooks/itext-7-converting-html-to-pdf-with-pdfhtml https://www.baeldung.com/java-pdf-creation
	 * https://www.baeldung.com/java-html-to-pdf
	 *
	 * @param entity
	 * @param storageBlockId
	 *
	 * @return
	 * @throws IOException
	 */
	@WithSpan
	public File getPdfInventorySheet(
		InteractingEntity entity,
		ObjectId storageBlockId,
		InventorySheetsOptions options
	) throws Throwable {
		log.info("Getting inventory sheet for block {} with options: {}", storageBlockId, options);
		
		StorageBlock block;
		List<StorageBlock> storageBlockChildren;
		SearchResult<InventoryItem> itemsInBlock;
		{
			CompletableFuture<StorageBlock> blockGetFut = CompletableFuture.supplyAsync(()->{
				return this.storageBlockService.get(storageBlockId);
			});
			CompletableFuture<List<StorageBlock>> blockChildrenGetFut =
				CompletableFuture.supplyAsync(()->{
					return this.storageBlockService.getChildrenIn(storageBlockId);
				});
			CompletableFuture<SearchResult<InventoryItem>> itemsInBlockGetFut = CompletableFuture.supplyAsync(()->{
				return new SearchResult<>(this.inventoryItemService.getItemsInBlock(storageBlockId));
			});
			
			try {
				block = blockGetFut.join();
				storageBlockChildren = blockChildrenGetFut.join();
				itemsInBlock = itemsInBlockGetFut.join();
			} catch(CompletionException e){
				throw e.getCause();
			}
		}
		
		File outputFile = getTempPdfFile(storageBlockId.toHexString());
		
		try (
			PdfWriter writer = new PdfWriter(outputFile);
		) {
			PdfDocument doc = new PdfDocument(writer);
			
			{
				PageSize size = new PageSize(options.getPageSize().size);
				
				if (PageOrientation.LANDSCAPE.equals(options.getPageOrientation())) {
					size = size.rotate();
				}
				doc.setDefaultPageSize(size);
			}
			
			doc.getDocumentInfo().addCreationDate();
			doc.getDocumentInfo().setCreator("Open QuarterMaster Base Station");
			doc.getDocumentInfo().setProducer("Open QuarterMaster Base Station");
			doc.getDocumentInfo().setAuthor(entity.getName() + " via Open QuarterMaster Base Station");
			doc.getDocumentInfo().setSubject("Inventory sheet for " + block.getLabel());
			doc.getDocumentInfo().setTitle(block.getLabelText() + " Inventory Sheet");
			doc.getDocumentInfo().setKeywords("inventory, sheet, " + storageBlockId);
			
			
			String html = this.getHtmlInventorySheet(
				block,
				null,
				storageBlockChildren,
				itemsInBlock,
				options
			).render();
			log.debug("Html generated: {}", html);
			HtmlConverter.convertToPdf(html, doc, CONVERTER_PROPERTIES);
		}
		return outputFile;
	}
}
