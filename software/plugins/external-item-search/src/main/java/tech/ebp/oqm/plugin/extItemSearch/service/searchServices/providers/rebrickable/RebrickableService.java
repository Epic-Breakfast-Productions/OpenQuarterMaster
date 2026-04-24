package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.rebrickable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Rebrickable search service.
 * <p>
 * API docs: - <a href="https://rebrickable.com/api/">...</a>
 */
@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class RebrickableService extends ItemSearchService {
	
	private static final String BRAND = "LEGO";
	
	RebrickableLookupClient rebrickableLookupClient;
	private String apiKey;
	private ObjectMapper objectMapper;
	
	@Inject
	public RebrickableService(
		@RestClient
		RebrickableLookupClient rebrickableLookupClient,
		@ConfigProperty(name = "productLookup.providers.rebrickable.enabled", defaultValue = "false")
		boolean enabled,
		@ConfigProperty(name = "productLookup.providers.rebrickable.apiKey", defaultValue = "")
		String apiKey,
		ObjectMapper objectMapper
	) {
		this.rebrickableLookupClient = rebrickableLookupClient;
		this.apiKey = apiKey;
		this.objectMapper = objectMapper;
		if (apiKey == null || apiKey.isBlank()) {
			log.warn("API key for Rebrickable was null or blank.");
			this.apiKey = null;
		} else {
			this.apiKey = apiKey;
		}
		super(
			enabled,
			LookupService.REBRICKABLE,
			ExtItemLookupProviderInfo
				.builder()
				.displayName("Rebrickable")
				.description("A database of LEGO(TM) pieces. Free, but requires you to get your own key.")
				.acceptsContributions(false)
				.cost("Free")
				.homepage(URI.create("https://rebrickable.com"))
		);
	}
	
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && this.apiKey != null && !this.apiKey.isBlank();
	}
	
	public LookupResult partJsonToResult(LookupSource source, LookupMethod method, ObjectNode results) {
		log.info("Search result: {}", results);
		ExtItemLookupResult.Builder<?, ?> resultBuilder = this.setupResponseBuilder(ExtItemLookupResult.builder(), source, method);
		
		List<String> images = new ArrayList<>();
		Map<String, String> links = new HashMap<>();
		Map<String, String> identifiers = new HashMap<>();
		Map<String, String> attributes = new HashMap<>();
		
		attributes.put("brand", BRAND);
		
		for (Map.Entry<String, JsonNode> curField : results.properties()) {
			String curFieldName = curField.getKey();
			JsonNode curFieldVal = curField.getValue();
			
			if(ResultMappingUtils.isFieldEmpty(curFieldVal)){
				continue;
			}
			
			switch (curFieldName) {
				case "name":
					resultBuilder.name(curFieldVal.asText());
					resultBuilder.unifiedName(curFieldVal.asText());
					break;
				case "part_num":
					identifiers.put("legoPartNum", curFieldVal.asText());
					break;
				case "part_img_url":
					images.add(curFieldVal.asText());
					break;
				case "part_url":
					links.put("rebrickable", curFieldVal.asText());
					break;
				case "external_ids":
					for (Map.Entry<String, JsonNode> curId : curFieldVal.properties()) {
						ArrayNode curIdArr = (ArrayNode) curId.getValue();
						if (curIdArr.isEmpty()) {
							continue;
						}
						if (curIdArr.size() == 1) {
							identifiers.put(curId.getKey(), curIdArr.get(0).asText());
						} else {
							//iterate over the array and add each value to the identifiers map
							int i = 0;
							for (JsonNode curIdNode : curIdArr) {
								identifiers.put(curId.getKey() + "-" + i++, curIdNode.asText());
							}
						}
					}
					break;
				default:
					if (curFieldVal.isValueNode()) {
						attributes.put(curFieldName, curFieldVal.asText());
					}
			}
		}
		
		resultBuilder.identifiers(identifiers);
		resultBuilder.images(images);
		resultBuilder.links(links);
		resultBuilder.attributes(attributes);
		
		return resultBuilder.build();
	}
	
	public Collection<LookupResult> partSearchJsonToResults(LookupSource source, LookupMethod method, ObjectNode results) {
		long count = results.get("count").asLong();
		
		if(count == 0){
			return List.of(this.setupResponseBuilder(LookupResultNoResults.builder(), source, method)
							   .detail("No results found.")
							   .build());
		}
		
		ArrayNode resultsAsArr = (ArrayNode) results.get("results");
		List<LookupResult> resultList = new ArrayList<>(resultsAsArr.size());
		
		for (JsonNode result : resultsAsArr) {
			resultList.add(this.partJsonToResult(source, method, (ObjectNode) result));
		}
		
		return resultList;
	}
	
	public LookupResult setJsonToResult(LookupSource source, LookupMethod method, ObjectNode results) {
		log.info("Search result: {}", results);
		ExtItemLookupResult.Builder<?, ?> resultBuilder = this.setupResponseBuilder(ExtItemLookupResult.builder(), source, method);
		
		List<String> images = new ArrayList<>();
		Map<String, String> links = new HashMap<>();
		Map<String, String> identifiers = new HashMap<>();
		Map<String, String> attributes = new HashMap<>();
		
		attributes.put("brand", BRAND);
		
		for (Map.Entry<String, JsonNode> curField : results.properties()) {
			String curFieldName = curField.getKey();
			JsonNode curFieldVal = curField.getValue();
			
			if(ResultMappingUtils.isFieldEmpty(curFieldVal)){
				continue;
			}
			
			switch (curFieldName) {
				case "name":
					resultBuilder.name(curFieldVal.asText());
					resultBuilder.unifiedName(curFieldVal.asText());
					break;
				case "set_num":
					identifiers.put("legoSetNum", curFieldVal.asText());
					break;
				case "set_img_url":
					images.add(curFieldVal.asText());
					break;
				case "set_url":
					links.put("rebrickable", curFieldVal.asText());
					break;
				default:
					if (curFieldVal.isValueNode()) {
						attributes.put(curFieldName, curFieldVal.asText());
					}
			}
		}
		
		resultBuilder.identifiers(identifiers);
		resultBuilder.images(images);
		resultBuilder.links(links);
		resultBuilder.attributes(attributes);
		
		return resultBuilder.build();
	}
	
	public Collection<LookupResult> setSearchJsonToResults(LookupSource source, LookupMethod method, ObjectNode results) {
		long count = results.get("count").asLong();
		
		if(count == 0){
			return List.of(this.setupResponseBuilder(LookupResultNoResults.builder(), source, method)
							   .detail("No results found.")
							   .build());
		}
		
		ArrayNode resultsAsArr = (ArrayNode) results.get("results");
		List<LookupResult> resultList = new ArrayList<>(resultsAsArr.size());
		
		for (JsonNode result : resultsAsArr) {
			resultList.add(this.setJsonToResult(source, method, (ObjectNode) result));
		}
		
		return resultList;
	}
	
	protected String getApiKey() {
		return "key " + this.apiKey;
	}
	
	@Override
	protected Multi<LookupResult> performSearch(LookupSource source, LookupMethod method, String term) {
		return switch (source) {
			case REBRICKABLE ->
				switch (method) {
					case PART_NUM -> this.rebrickableLookupClient.partFromNum(this.getApiKey(), term)
										 .map(result->this.partJsonToResult(source, method, result))
										 .onFailure().recoverWithItem(e->this.handleError(source, method, e))
										 .toMulti();
					
					case SET_NUM -> this.rebrickableLookupClient.setFromNum(this.getApiKey(), term)
										.map(result->this.partJsonToResult(source, method, result))
										.onFailure().recoverWithItem(e->this.handleError(source, method, e))
										.toMulti();
					case TEXT -> Multi.createBy().merging().streams(
						this.rebrickableLookupClient.partsSearch(this.getApiKey(), term)
							.map(result->this.partSearchJsonToResults(source, method, result))
							.onFailure().recoverWithItem(e->this.handleErrorRetCollection(source, method, e))
							.onItem().transformToMulti(collection-> Multi.createFrom().iterable(collection)),
						this.rebrickableLookupClient.setsSearch(this.getApiKey(), term)
							.map(result->this.setSearchJsonToResults(source, method, result))
							.onFailure().recoverWithItem(e->this.handleErrorRetCollection(source, method, e))
							.onItem().transformToMulti(collection-> Multi.createFrom().iterable(collection))
					);
					default -> throw new IllegalArgumentException("Invalid lookup method: " + method);
				};
			default -> throw new IllegalArgumentException("Invalid lookup source: " + source);
		};
	}
	
	@Override
	protected Optional<LookupResult> handleClientError(LookupSource source, LookupMethod method, ClientWebApplicationException e) {
		if (e.getResponse().getStatus() == 404) {
			try {
				ObjectNode errorDeets = (ObjectNode) this.objectMapper.readTree((InputStream) e.getResponse().getEntity());
				
				if (errorDeets.get("detail").asText().equals("No Part matches the given query.")) {
					return Optional.of(
						this.setupResponseBuilder(LookupResultNoResults.builder(), source, method)
							.detail("No Part matches the given query.")
							.build()
					);
				}
			} catch(Throwable e2) {
				log.warn("Error parsing error response from Rebrickable: {}", e2.getMessage(), e2);
			}
		}
		return Optional.empty();
	}
}
