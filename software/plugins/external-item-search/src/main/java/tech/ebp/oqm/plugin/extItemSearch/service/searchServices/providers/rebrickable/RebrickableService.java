package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.rebrickable;

import com.fasterxml.jackson.databind.JsonNode;
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
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.ItemKind;

import java.net.URI;
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
	
	@Inject
	public RebrickableService(
		@RestClient
		RebrickableLookupClient rebrickableLookupClient,
		@ConfigProperty(name = "productLookup.providers.rebrickable.enabled", defaultValue = "false")
		boolean enabled,
		@ConfigProperty(name = "productLookup.providers.rebrickable.homepage", defaultValue = "")
		String apiKey
	) {
		this.rebrickableLookupClient = rebrickableLookupClient;
		this.apiKey = apiKey;
		
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
	public LookupResult jsonNodeToSearchResults(ObjectNode results) {
		log.info("Search result: {}", results);
		ExtItemLookupResult.Builder<?, ?> resultBuilder = ExtItemLookupResult.builder()
															  .source(this.getProviderInfo().getDisplayName())
															  .brand(BRAND);
		
		Map<String, String> attributes = new HashMap<>();
		
		for (Map.Entry<String, JsonNode> curField : results.properties()) {
			String curFieldName = curField.getKey();
			String curFieldVal = curField.getValue().asText();
			
			//TODO:: handle images
			
			if (curField.getValue().isNull() || curFieldVal == null || curFieldVal.isBlank()) {
				continue;
			}
			
			switch (curFieldName) {
				case "name":
					resultBuilder.name(curFieldVal);
					resultBuilder.unifiedName(curFieldVal);
					break;
				default:
					attributes.put(curFieldName, curFieldVal);
			}
		}
		
		resultBuilder.attributes(attributes);
		
		return resultBuilder.build();
	}
	
	@CacheResult(cacheName = "rebrickable-part-num-search")
	public Uni<ObjectNode> performPartNoSearch(String partNum) {
		return this.rebrickableLookupClient.getFromPartNum(this.apiKey, partNum);
	}
	
	@Override
	public Optional<Multi<LookupResult>> searchPartNum(String partNum) {
		return Optional.of(
			this.performPartNoSearch(partNum)
			.map(this::jsonNodeToSearchResults)
				.toMulti()
		);
	}
}
