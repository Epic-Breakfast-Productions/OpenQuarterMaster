package tech.ebp.oqm.baseStation.service.productLookup.searchServices.api.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.baseStation.rest.restCalls.productLookup.api.DataKickLookupClient;
import tech.ebp.oqm.baseStation.model.rest.externalItemLookup.ExtItemLookupProviderInfo;
import tech.ebp.oqm.baseStation.model.rest.externalItemLookup.ExtItemLookupResult;

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
@NoArgsConstructor
public class DataKickService extends ApiProductSearchService {
	
	@Inject
	@RestClient
	DataKickLookupClient dataKickLookupClient;
	@Getter
	ExtItemLookupProviderInfo providerInfo;
	
	@Inject
	public DataKickService(
		@RestClient
		DataKickLookupClient dataKickLookupClient,
		@ConfigProperty(name = "productLookup.providers.datakick.displayName")
		String displayName,
		@ConfigProperty(name = "productLookup.providers.datakick.enabled", defaultValue = "false")
		boolean enabled,
		@ConfigProperty(name = "productLookup.providers.datakick.description", defaultValue = "")
		String description,
		@ConfigProperty(name = "productLookup.providers.datakick.acceptsContributions", defaultValue = "")
		boolean acceptsContributions,
		@ConfigProperty(name = "productLookup.providers.datakick.homepage", defaultValue = "")
		URL homepage,
		@ConfigProperty(name = "productLookup.providers.datakick.cost", defaultValue = "")
		String cost
	) {
		this.dataKickLookupClient = dataKickLookupClient;
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
	
	/**
	 * https://gtinsearch.org/api
	 * https://www.gtinsearch.org/api/items/0754523765792
	 *
	 * @param results
	 *
	 * @return
	 */
	@WithSpan
	@Override
	public List<ExtItemLookupResult> jsonNodeToSearchResults(JsonNode results) {
		log.debug("Data from Datakick: {}", results.toPrettyString());
		if (!results.isArray()) {
			log.warn("Data from DataKick not an array!");
			return List.of();
		}
		
		ArrayNode resultsAsArr = (ArrayNode) results;
		List<ExtItemLookupResult> resultList = new ArrayList<>(resultsAsArr.size());
		
		for (JsonNode result : results) {
			ObjectNode curResultJson = (ObjectNode) result;
			String brandName = "";
			String name = "";
			Map<String, String> attributes = new HashMap<>();
			
			
			for (Iterator<Map.Entry<String, JsonNode>> iter = curResultJson.fields(); iter.hasNext(); ) {
				Map.Entry<String, JsonNode> curField = iter.next();
				String curFieldName = curField.getKey();
				String curFieldVal = curField.getValue().asText();
				
				if (curField.getValue().isNull() || curFieldVal == null || curFieldVal.isBlank()) {
					continue;
				}
				
				switch (curFieldName) {
					case "id":
					case "user_id":
					case "created_at":
					case "updated_at":
						continue;
					case "brand_name":
						brandName = curFieldVal;
						break;
					case "name":
						name = curFieldVal;
						break;
					default:
						attributes.put(curFieldName, curFieldVal);
				}
			}
			
			resultList.add(
				ExtItemLookupResult
					.builder()
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
		return this.dataKickLookupClient.getFromUpcCode(barcode);
	}
}
