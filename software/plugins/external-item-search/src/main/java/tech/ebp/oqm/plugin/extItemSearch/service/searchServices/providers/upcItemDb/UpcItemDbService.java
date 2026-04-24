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
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResultNoResults;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupSource;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.ResultMappingUtils;

import java.net.URI;
import java.util.*;

import static tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupMethod.BARCODE;

/**
 *
 * - Docs: https://www.upcitemdb.com/wp/docs/main/development/getting-started/ - Trial docs: https://www.upcitemdb.com/api/explorer#!/lookup/get_trial_lookup
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
		super(
			enabled,
			LookupService.UPC_ITEM_DB,
			ExtItemLookupProviderInfo
				.builder()
				.displayName("upcitemdb.com")
				.description("A lookup database with good number of records, and a free tier with 100 requests per day.")
				.acceptsContributions(false)
				.homepage(URI.create("https://www.upcitemdb.com/"))
				.cost("Paid, Free tier")
				.enabled(enabled)
		);
	}
	
	public boolean hasKey() {
		return this.apiKey != null && !this.apiKey.isBlank();
	}
	
	private ExtItemLookupResult jsonToResult(LookupSource source, LookupMethod method, ObjectNode json) {
		ExtItemLookupResult.Builder<?, ?> resultBuilder = this.setupResponseBuilder(ExtItemLookupResult.builder(), source, method);
		
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
					if (curFieldVal.isTextual()) {
						attributes.put(curFieldName, curFieldVal.asText());
					}
					break;
			}
		}
		
		return resultBuilder
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
	public Collection<LookupResult> jsonNodeToSearchResults(LookupSource source, LookupMethod type, ObjectNode results) {
		log.debug("Data from upcitemdb: {}", results.toPrettyString());
		
		ArrayNode resultsAsArr = (ArrayNode) results.get("items");
		List<LookupResult> resultList = new ArrayList<>(resultsAsArr.size());
		
		for (JsonNode result : resultsAsArr) {
			resultList.add(this.jsonToResult(source, type, (ObjectNode) result));
		}
		
		return resultList;
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
	
	@Override
	protected Multi<LookupResult> performSearch(LookupSource source, LookupMethod method, String term) {
		return switch (source) {
			case UPC_ITEM_DB ->
				switch (method) {
					case BARCODE -> this.performBarcodeSearchCall(term)
										.map(result->this.jsonNodeToSearchResults(source, method, result))
										.onFailure().recoverWithItem(e->this.handleErrorRetCollection(source, method, e))
										.onItem().transformToMulti(collection->
																	   Multi.createFrom().iterable(collection)
						);
					default -> throw new IllegalArgumentException("Invalid lookup method: " + method);
				};
			default -> throw new IllegalArgumentException("Invalid lookup source: " + source);
		};
	}
	
	@Override
	protected Optional<LookupResult> handleClientError(LookupSource source, LookupMethod method, ClientWebApplicationException e) {
		if (e.getResponse().getStatus() == 404) {
			return Optional.of(
				this.setupResponseBuilder(LookupResultNoResults.builder(), source, method)
					.detail("No results found.")
					.build()
			);
		}
		return Optional.empty();
	}
}
