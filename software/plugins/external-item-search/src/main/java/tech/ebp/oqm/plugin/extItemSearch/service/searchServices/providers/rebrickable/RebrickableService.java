package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.rebrickable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
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
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.ItemKind;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupType;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
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
	
	@Getter
	ExtItemLookupProviderInfo providerInfo;
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
		
		ExtItemLookupProviderInfo.Builder infoBuilder = ExtItemLookupProviderInfo
															.builder()
															.id("rebrickable")
															.displayName("Rebrickable")
															.description("A database of LEGO(TM) pieces. Free, but requires you to get your own key.")
															.acceptsContributions(false)
															.cost("Free")
															.brands(List.of(BRAND))
															.kinds(List.of(ItemKind.LEGO))
															.homepage(URI.create("https://rebrickable.com"));
		
		if (apiKey == null || apiKey.isBlank()) {
			log.warn("API key for Rebrickable was null or blank.");
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
		return this.providerInfo.isEnabled() && this.apiKey != null && !this.apiKey.isBlank();
	}
	
	@WithSpan
	public LookupResult jsonNodeToSearchResults(LookupType type, ObjectNode results) {
		log.info("Search result: {}", results);
		ExtItemLookupResult.Builder<?, ?> resultBuilder = ExtItemLookupResult.builder()
															  .lookupType(type)
															  .source(this.getProviderInfo().getDisplayName())
															  .brand(BRAND);
		
		List<String> images = new ArrayList<>();
		Map<String, String> links = new HashMap<>();
		Map<String, String> identifiers = new HashMap<>();
		Map<String, String> attributes = new HashMap<>();
		
		for (Map.Entry<String, JsonNode> curField : results.properties()) {
			String curFieldName = curField.getKey();
			JsonNode curFieldVal = curField.getValue();
			
			if (curField.getValue().isNull() || curFieldVal == null) {
				continue;
			}
			if (curFieldVal.isTextual() && curFieldVal.asText().isBlank()) {
				continue;
			}
			if (curFieldVal.isArray() && curFieldVal.isEmpty()) {
				continue;
			}
			if (curFieldVal.isObject() && curFieldVal.isEmpty()) {
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
	
	protected String getApiKey() {
		return "key " + this.apiKey;
	}
	
	@Override
	public Optional<Multi<LookupResult>> searchPartNum(String partNum) {
		return Optional.of(
			this.rebrickableLookupClient.getFromPartNum(this.getApiKey(), partNum)
				.map(result->this.jsonNodeToSearchResults(LookupType.PART_NUM, result))
				.onFailure().recoverWithItem(e->this.handleError(LookupType.PART_NUM, e))
				.toMulti()
		);
	}
	
	@Override
	protected Optional<LookupResult> handleClientError(LookupType type, ClientWebApplicationException e) {
		if (e.getResponse().getStatus() == 404) {
			try {
				ObjectNode errorDeets = (ObjectNode) this.objectMapper.readTree((InputStream) e.getResponse().getEntity());
				
				if (errorDeets.get("detail").asText().equals("No Part matches the given query.")) {
					return Optional.of(
						this.setupResponseBuilder(LookupResultNoResults.builder(), type)
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
