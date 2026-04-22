package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.upcItemDb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupType;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.ResultMappingUtils;

import java.net.URI;
import java.util.*;

/**
 *
 * - Docs: https://www.upcitemdb.com/wp/docs/main/development/getting-started/
 * - Trial docs: https://www.upcitemdb.com/api/explorer#!/lookup/get_trial_lookup
 */
@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class UpcItemDbService extends ItemSearchService {
	
	@Inject
	@RestClient
	UpcItemDbLookupClient upcItemDbLookupClient;
	@Getter
	ExtItemLookupProviderInfo providerInfo;
	
	private String apiKey;
	
	@Inject
	public UpcItemDbService(
		@RestClient
		UpcItemDbLookupClient upcItemDbLookupClient,
		@ConfigProperty(name = "productLookup.providers.upcitemdb.enabled", defaultValue = "false")
		boolean enabled,
		@ConfigProperty(name = "productLookup.providers.upcitemdb.apiKey", defaultValue = "")
		String apiKey
	) {
		this.upcItemDbLookupClient = upcItemDbLookupClient;
		this.apiKey = apiKey;
		this.providerInfo = ExtItemLookupProviderInfo
								.builder()
								.id("upcitemdb-com")
								.displayName("upcitemdb.com")
								.description("A lookup database with good number of records, and a free tier with 100 requests per day.")
								.acceptsContributions(false)
								.homepage(URI.create("https://www.upcitemdb.com/"))
								.cost("Paid, Free tier")
								.enabled(enabled)
								.build();
	}
	
	@Override
	public boolean isEnabled() {
		return this.getProviderInfo().isEnabled();
	}
	
	public boolean hasKey() {
		return this.apiKey != null && !this.apiKey.isBlank();
	}
	
	private ExtItemLookupResult jsonToResult(LookupType type, ObjectNode json) {
		ExtItemLookupResult.Builder<?, ?> resultBuilder = this.setupResponseBuilder(ExtItemLookupResult.builder(), type);
		
		Map<String, String> attributes = new HashMap<>();
		Map<String, String> identifiers = new HashMap<>();
		List<String> images = new ArrayList<>();
		
		for (Map.Entry<String, JsonNode> curField : json.properties()) {
			String curFieldName = curField.getKey();
			JsonNode curFieldVal = curField.getValue();
			
			if (ResultMappingUtils.isFieldEmpty(curFieldVal)) {
				continue;
			}
			
			switch (curFieldName) {
				case "ean":
				case "upc":
				case "asin":
				case "elid":
					identifiers.put(curFieldName, curFieldVal.asText());
					break;
				case "title":
					resultBuilder.name(curFieldVal.asText());
					resultBuilder.unifiedName(curFieldVal.asText());
					break;
				case "description":
					resultBuilder.description(curFieldVal.asText());
					break;
				case "images":
					for (JsonNode curImg : curFieldVal) {
						images.add(curImg.asText());
					}
					break;
				default:
					if(curFieldVal.isTextual()){
						attributes.put(curFieldName, curFieldVal.asText());
					}
					break;
			}
		}
		
		return resultBuilder
				   .source(this.getProviderInfo().getDisplayName())
				   .attributes(attributes)
				   .identifiers(identifiers)
				   .images(images)
				   .build();
	}
	
	/**
	 * https://www.upcitemdb.com/wp/docs/main/development/responses/
	 *
	 * @param results
	 *
	 * @return
	 */
	@WithSpan
	public Collection<LookupResult> jsonNodeToSearchResults(LookupType type, ObjectNode results) {
		log.debug("Data from upcitemdb: {}", results.toPrettyString());
		
		ArrayNode resultsAsArr = (ArrayNode) results.get("items");
		List<LookupResult> resultList = new ArrayList<>(resultsAsArr.size());
		
		for (JsonNode result : resultsAsArr) {
			resultList.add(this.jsonToResult(type, (ObjectNode) result));
		}
		
		return resultList;
	}
	
	@Override
	public Optional<Multi<LookupResult>> searchBarcode(String barcode) {
		return Optional.of(
			this.performBarcodeSearchCall(barcode)
				.map(result->this.jsonNodeToSearchResults(LookupType.BARCODE, result))
				.onFailure().recoverWithItem(e->this.handleErrorRetCollection(LookupType.BARCODE, e))
				.onItem().transformToMulti(collection->
											   Multi.createFrom().iterable(collection)
				)
		);
	}
	
	
	protected Uni<ObjectNode> performBarcodeSearchCall(String barcode) {
		if (this.hasKey()) {
			return this.upcItemDbLookupClient.getFromUpcCode(
				this.apiKey,
				"3scale",
				UpcItemDbLookupClient.Request.builder().upc(barcode).build()
			);
		}
		
		return this.upcItemDbLookupClient.getFromUpcCodeTrial(barcode);
	}
}
