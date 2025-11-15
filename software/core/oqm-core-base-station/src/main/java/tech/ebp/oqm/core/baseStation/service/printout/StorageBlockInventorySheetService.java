package tech.ebp.oqm.core.baseStation.service.printout;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.model.UserInfo;
import tech.ebp.oqm.core.baseStation.model.printouts.InventorySheetsOptions;
import tech.ebp.oqm.core.baseStation.model.printouts.PageOrientation;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

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

	@RestClient
	OqmCoreApiClientService coreApiClientService;

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
		ObjectNode storageBlock,
		ObjectNode childrenSr,
		ObjectNode itemsInBlockSr,
		InventorySheetsOptions options
	) {
		//TODO:: update these
		Predicate<ObjectNode> simpleAmountFilter = new Predicate<ObjectNode>() {
			@Override
			public boolean test(ObjectNode inventoryItem) {
				return "AMOUNT_SIMPLE".equals(inventoryItem.get("storageType").asText());
			}
		};
		Predicate<ObjectNode> listAmountFilter = new Predicate<ObjectNode>() {
			@Override
			public boolean test(ObjectNode inventoryItem) {
				return "AMOUNT_LIST".equals(inventoryItem.get("storageType").asText());
			}
		};
		Predicate<ObjectNode> trackedFilter = new Predicate<ObjectNode>() {
			@Override
			public boolean test(ObjectNode inventoryItem) {
				return  "TRACKED".equals(inventoryItem.get("storageType").asText());
			}
		};
		
		return this.setupBasicPrintoutData(this.inventorySheetTemplate)
			.data("simpleAmountFilter", simpleAmountFilter)
			.data("listAmountFilter", listAmountFilter)
			.data("trackedFilter", trackedFilter)
			.data("options", options)
			.data("storageBlock", storageBlock)
			.data("storageBlockChildrenSearchResults", childrenSr)
			.data("searchResult", itemsInBlockSr)
			;
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
		UserInfo entity,
		String oqmApiToken,
		String oqmDbIdOrName,
		String storageBlockId,
		InventorySheetsOptions options
	) throws Throwable {
		log.info("Getting inventory sheet for block {} with options: {}", storageBlockId, options);

		ObjectNode block;
		ObjectNode storageBlockChildrenSr;
		ObjectNode itemsInBlockSr;
		{
			CompletableFuture<ObjectNode> blockGetFut = this.coreApiClientService.storageBlockGet(oqmApiToken, oqmDbIdOrName, storageBlockId)
				.subscribeAsCompletionStage();
			CompletableFuture<ObjectNode> blockChildrenGetFut = this.coreApiClientService.storageBlockSearch(
				oqmApiToken, oqmDbIdOrName,
				StorageBlockSearch.builder().isChildOf(storageBlockId).build()
			)
				.subscribeAsCompletionStage();
			CompletableFuture<ObjectNode> itemsInBlockGetFut = this.coreApiClientService.invItemSearch(
				oqmApiToken,
				oqmDbIdOrName,
				InventoryItemSearch.builder().inStorageBlocks(List.of(storageBlockId)).build()
			).subscribeAsCompletionStage();

			try {
				block = blockGetFut.join();
				storageBlockChildrenSr = blockChildrenGetFut.join();
				itemsInBlockSr = itemsInBlockGetFut.join();
			} catch(CompletionException e){
				throw e.getCause();
			}
		}

		File outputFile = getTempPdfFile(storageBlockId);

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
			doc.getDocumentInfo().setSubject("Inventory sheet for " + block.get("label").asText());
			doc.getDocumentInfo().setTitle(block.get("labelText").asText() + " Inventory Sheet");
			doc.getDocumentInfo().setKeywords("inventory, sheet, " + storageBlockId);
			
			String html = this.getHtmlInventorySheet(
				block,
				storageBlockChildrenSr,
				itemsInBlockSr,
				options
			).render();
			log.debug("Html generated: {}", html);
			HtmlConverter.convertToPdf(html, doc, CONVERTER_PROPERTIES);
		}
		return outputFile;
	}
}