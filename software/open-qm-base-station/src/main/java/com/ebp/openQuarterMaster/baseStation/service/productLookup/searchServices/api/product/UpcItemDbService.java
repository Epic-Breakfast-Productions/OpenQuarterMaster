package com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.product;

import com.ebp.openQuarterMaster.baseStation.rest.restCalls.productLookup.api.UpcItemDbLookupClient;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupProviderInfo;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Slf4j
@Traced
@NoArgsConstructor
public class UpcItemDbService extends ApiProductSearchService {
	
	@Inject
	@RestClient
	UpcItemDbLookupClient upcItemDbLookupClient;
	@Getter
	ProductLookupProviderInfo providerInfo;
	
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
		@ConfigProperty(name = "productLookup.providers.upcitemdb.key", defaultValue = "")
		String apiKey
	) {
		this.upcItemDbLookupClient = upcItemDbLookupClient;
		this.apiKey = apiKey;
		this.providerInfo = ProductLookupProviderInfo
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
	
	public boolean hasKey(){
		return this.apiKey != null && !this.apiKey.isBlank();
	}
	
	/**
	 * https://www.upcitemdb.com/wp/docs/main/development/responses/
	 *
	 * @param results
	 *
	 * @return
	 */
	@Override
	public List<ProductLookupResult> jsonNodeToSearchResults(JsonNode results) {
		log.debug("Data from upcitemdb: {}", results.toPrettyString());
		
		ArrayNode resultsAsArr = (ArrayNode) results.get("items");
		List<ProductLookupResult> resultList = new ArrayList<>(resultsAsArr.size());
		
		for (JsonNode result : resultsAsArr) {
			ObjectNode curResultJson = (ObjectNode) result;
			String brandName = "";
			String name = "";
			Map<String, String> attributes = new HashMap<>();
			ProductLookupResult.Builder<?,?> resultBuilder = ProductLookupResult.builder();
			
			for (Iterator<Map.Entry<String, JsonNode>> iter = curResultJson.fields(); iter.hasNext(); ) {
				Map.Entry<String, JsonNode> curField = iter.next();
				String curFieldName = curField.getKey();
				String curFieldVal = curField.getValue().asText();
				
				//TODO:: handle images
				
				if (curField.getValue().isNull() || curFieldVal == null || curFieldVal.isBlank()) {
					continue;
				}
				
				switch (curFieldName) {
					case "brand":
						brandName = curFieldVal;
						break;
					case "title":
						name = curFieldVal;
						break;
					case "description":
						resultBuilder.description(curFieldVal);
						break;
					default:
						attributes.put(curFieldName, curFieldVal);
				}
			}
			
			resultList.add(
				resultBuilder
					.source(this.getProviderInfo().getDisplayName())
					.name(name)
					.brand(brandName)
					.unifiedName(name)
					.attributes(attributes)
					.build()
			);
		}
		
		return resultList;
	}
	
	
	
	@Override
	protected CompletionStage<JsonNode> performBarcodeSearchCall(String barcode) {
		if(this.hasKey()){
			return this.upcItemDbLookupClient.getFromUpcCode(
				this.apiKey,
				"3scale",
				UpcItemDbLookupClient.Request.builder().upc(barcode).build()
			);
		}
		
		return this.upcItemDbLookupClient.getFromUpcCodeTrial(barcode);
	}
}
