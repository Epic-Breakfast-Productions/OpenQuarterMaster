package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.barcodeLookup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResultNoResults;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupType;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.ResultMappingUtils;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 *
 * - https://www.barcodelookup.com/api
 */
@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class BarcodeLookupService extends ItemSearchService {
	
	BarcodeLookupClient barcodeLookupClient;
	@Getter
	ExtItemLookupProviderInfo providerInfo;
	String apiKey;
	
	@Inject
	public BarcodeLookupService(
		@RestClient
		BarcodeLookupClient barcodeLookupClient,
		@ConfigProperty(name = "productLookup.providers.barcodelookup-com.enabled", defaultValue = "false")
		boolean enabled,
		@ConfigProperty(name = "productLookup.providers.barcodelookup-com.apiKey", defaultValue = "")
		String apiKey
	) {
		this.barcodeLookupClient = barcodeLookupClient;
		
		ExtItemLookupProviderInfo.Builder infoBuilder = ExtItemLookupProviderInfo
															.builder()
															.id("barcodelookup-com")
															.displayName("BarcodeLookup.com")
															.description("Comprehensive database of products, but a paid service. Can get a 2-week trial API key.")
															.acceptsContributions(true)
															.homepage(URI.create("https://www.barcodelookup.com/"))
															.cost("Paid");
		
		if (apiKey == null || apiKey.isBlank()) {
			log.warn("API key for BarcodeLookup was null or blank.");
			infoBuilder.enabled(false);
			this.apiKey = null;
		} else {
			infoBuilder.enabled(enabled);
			this.apiKey = apiKey;
		}
		
		this.providerInfo = infoBuilder.build();
	}
	
	@Override
	public boolean isEnabled() {
		return this.getProviderInfo().isEnabled();
	}
	
	/**
	 * https://www.barcodelookup.com/api
	 *
	 * @param results
	 *
	 * @return
	 */
	public Collection<LookupResult> jsonNodeToSearchResults(LookupType type, JsonNode results) {
		log.debug("Data from BarcodeLookup: {}", results.toPrettyString());
		
		ArrayNode resultsAsArr = (ArrayNode) results.get("products");
		List<LookupResult> resultList = new ArrayList<>(resultsAsArr.size());
		
		for (JsonNode result : resultsAsArr) {
			ObjectNode curResultJson = (ObjectNode) result;
			ExtItemLookupResult.Builder<?, ?> resultBuilder = this.setupResponseBuilder(ExtItemLookupResult.builder(), type);
			
			
			Map<String, String> attributes = new HashMap<>();
			Map<String, String> identifiers = new HashMap<>();
			Map<String, String> links = new HashMap<>();
			List<String> images = new ArrayList<>();
			
			for (Iterator<Map.Entry<String, JsonNode>> iter = curResultJson.fields(); iter.hasNext(); ) {
				Map.Entry<String, JsonNode> curField = iter.next();
				
				String curFieldName = curField.getKey();
				JsonNode curFieldVal = curField.getValue();
				
				if(ResultMappingUtils.isFieldEmpty(curFieldVal)){
					continue;
				}
				
				switch (curFieldName) {
					case "brand":
					case "asin":
					case "mpn":
					case "model":
						identifiers.put(curFieldName, curFieldVal.asText());
						break;
					case "barcode_formats":
						for(String curBarcode : curFieldVal.asText().split(", ")){
							String[] barcodeParts = curBarcode.split(" ");
							identifiers.put(barcodeParts[0], barcodeParts[1]);
						}
						break;
					case "title":
						resultBuilder.name(curFieldVal.asText());
						resultBuilder.unifiedName(curFieldVal.asText());
						break;
					case "description":
						resultBuilder.description(curFieldVal.asText());
						break;
					case "images":
						images.addAll(
							curFieldVal.valueStream().map(JsonNode::asText).toList()
						);
						break;
					case "stores":
						curFieldVal.valueStream()
							.forEach(curStoreJson -> {
								links.put(curStoreJson.get("name").asText(), curStoreJson.get("link").asText());
							});
						break;
					default:
						if(curFieldVal.isTextual()){
							attributes.put(curFieldName, curFieldVal.asText());
						}
				}
			}
			
			resultList.add(
				resultBuilder
					.attributes(attributes)
					.identifiers(identifiers)
					.links(links)
					.images(images)
					.build()
			);
		}
		
		return resultList;
	}
	
	@Override
	public Optional<Multi<LookupResult>> searchBarcode(String search) {
		return Optional.of(
			this.barcodeLookupClient.searchBarcode(this.apiKey, search)
				.map(results->this.jsonNodeToSearchResults(LookupType.BARCODE, results))
				.onFailure().recoverWithItem(e->this.handleErrorRetCollection(LookupType.BARCODE, e))
				
				.onItem().transformToMulti(collection->
											   Multi.createFrom().iterable(collection)
				)
		);
	}
	
	@Override
	protected Optional<LookupResult> handleClientError(LookupType type, ClientWebApplicationException e) {
		if (e.getResponse().getStatus() == 404) {
			return Optional.of(
				this.setupResponseBuilder(LookupResultNoResults.builder(), type)
					.detail("No items found.")
					.build()
			);
		}
		return Optional.empty();
	}
}
