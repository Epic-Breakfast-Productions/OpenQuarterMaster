package tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.api.product.upcItemDb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.openQuarterMaster.plugin.productSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.openQuarterMaster.plugin.productSearch.model.ExtItemLookupResult;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.api.product.ApiProductSearchService;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 *
 * - Docs: https://www.upcitemdb.com/wp/docs/main/development/getting-started/
 * - Trial docs: https://www.upcitemdb.com/api/explorer#!/lookup/get_trial_lookup
 */
@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class UpcItemDbService extends ApiProductSearchService {
	
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
		@ConfigProperty(name = "productLookup.providers.upcitemdb.displayName")
		String displayName,
		@ConfigProperty(name = "productLookup.providers.upcitemdb.enabled", defaultValue = "false")
		boolean enabled,
		@ConfigProperty(name = "productLookup.providers.upcitemdb.description", defaultValue = "")
		String description,
		@ConfigProperty(name = "productLookup.providers.upcitemdb.acceptsContributions", defaultValue = "")
		boolean acceptsContributions,
		@ConfigProperty(name = "productLookup.providers.upcitemdb.homepage", defaultValue = "")
		URL homepage,
		@ConfigProperty(name = "productLookup.providers.upcitemdb.cost", defaultValue = "")
		String cost,
		@ConfigProperty(name = "productLookup.providers.upcitemdb.apiKey", defaultValue = "")
		String apiKey
	) {
		this.upcItemDbLookupClient = upcItemDbLookupClient;
		this.apiKey = apiKey;
		this.providerInfo = ExtItemLookupProviderInfo
								.builder()
								.displayName(displayName)
								.enabled(enabled)
								.description(description)
								.acceptsContributions(acceptsContributions)
								.homepage(homepage)
								.cost(cost)
								.build();
	}
	
	@Override
	public boolean isEnabled() {
		return this.getProviderInfo().isEnabled();
	}
	
	public boolean hasKey() {
		return this.apiKey != null && !this.apiKey.isBlank();
	}
	
	private ExtItemLookupResult jsonToResult(ObjectNode json) {
		String brandName = "";
		String name = "";
		Map<String, String> attributes = new HashMap<>();
		ExtItemLookupResult.Builder<?, ?> resultBuilder = ExtItemLookupResult.builder();
		
		
		for (Iterator<Map.Entry<String, JsonNode>> iter = json.fields(); iter.hasNext(); ) {
			Map.Entry<String, JsonNode> curField = iter.next();
			String curFieldName = curField.getKey();
			JsonNode curFieldVal = curField.getValue();
			
			
			if (curField.getValue().isNull() || curFieldVal == null) {
				continue;
			}
			
			switch (curFieldName) {
				case "ean":
					resultBuilder.barcode(curFieldVal.asText());
					break;
				case "brand":
					brandName = curFieldVal.asText();
					break;
				case "title":
					name = curFieldVal.asText();
					break;
				case "description":
					resultBuilder.description(curFieldVal.asText());
					break;
				case "images":
					ArrayList<String> images = new ArrayList<>(curFieldVal.size());
					for (JsonNode curImg : (ArrayNode) curFieldVal) {
						images.add(curImg.asText());
					}
					resultBuilder.images(images);
					break;
				default: {
					String text = curFieldVal.asText();
					if (!text.isBlank()) {
						attributes.put(curFieldName, text);
					}
					break;
				}
			}
		}
		
		return resultBuilder
				   .source(this.getProviderInfo().getDisplayName())
				   .name(name)
				   .brand(brandName)
				   .unifiedName((brandName != null && !brandName.isBlank() ? brandName + " " + name : name))
				   .attributes(attributes)
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
	@Override
	public List<ExtItemLookupResult> jsonNodeToSearchResults(JsonNode results) {
		log.debug("Data from upcitemdb: {}", results.toPrettyString());
		
		ArrayNode resultsAsArr = (ArrayNode) results.get("items");
		List<ExtItemLookupResult> resultList = new ArrayList<>(resultsAsArr.size());
		
		for (JsonNode result : resultsAsArr) {
			resultList.add(this.jsonToResult((ObjectNode) result));
		}
		
		return resultList;
	}
	
	@WithSpan
	@Override
	protected CompletionStage<JsonNode> performBarcodeSearchCall(String barcode) {
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
