package tech.ebp.oqm.baseStation.service.printouts;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.printouts.InventorySheetsOptions;
import tech.ebp.oqm.baseStation.rest.printouts.PageOrientation;
import tech.ebp.oqm.baseStation.service.mongo.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Traced
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
	@Location("printouts/storageBlockInventorySheet.html")
	Template inventorySheetTemplate;
	
	
	private TemplateInstance getHtmlInventorySheet(StorageBlock storageBlock, List<InventoryItem> itemsInBlock, InventorySheetsOptions options) {
		return this.setupBasicPrintoutData(this.inventorySheetTemplate)
				   .data("options", options)
				   .data("storageBlock", storageBlock)
				   .data("itemsInBlock", itemsInBlock)
				   .data("imageService", this.imageService);
	}
	
	/**
	 * https://kb.itextpdf.com/home/it7kb/ebooks/itext-7-converting-html-to-pdf-with-pdfhtml
	 * @param entity
	 * @param storageBlockId
	 * @return
	 * @throws IOException
	 */
	public File getPdfInventorySheet(InteractingEntity entity, ObjectId storageBlockId, InventorySheetsOptions options) throws IOException {
		log.info("Getting inventory sheet for block {} with options: {}", storageBlockId, options);
		StorageBlock block = this.storageBlockService.get(storageBlockId);
		List<InventoryItem> itemsInBlock = this.inventoryItemService.getItemsInBlock(storageBlockId);
		
		java.nio.file.Path tempDirPath = Files.createTempDirectory(EXPORT_TEMP_DIR_PREFIX);
		File tempDir = tempDirPath.toFile();
		tempDir.deleteOnExit();
		String exportFileName =
			"oqm_storage_sheet_" + storageBlockId.toHexString() + "_" + ZonedDateTime.now().format(FILENAME_TIMESTAMP_FORMAT) + ".pdf";
		File outputFile = new File(tempDir, exportFileName);
		
		try (
			PdfWriter writer = new PdfWriter(outputFile);
		) {
			PdfDocument doc = new PdfDocument(writer);
			
			{
				PageSize size = new PageSize(options.getPageSize().size);
				
				if(PageOrientation.LANDSCAPE.equals(options.getPageOrientation())){
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
			
			String html = this.getHtmlInventorySheet(block, itemsInBlock, options).render();
			log.debug("Html generated: {}", html);
			HtmlConverter.convertToPdf(html, doc, CONVERTER_PROPERTIES);
		}
		return outputFile;
	}
}
