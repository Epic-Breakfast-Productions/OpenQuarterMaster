package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.dataKick;

import com.fasterxml.jackson.databind.JsonNode;
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
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupType;

import java.net.URI;
import java.util.*;

/**
 * - https://gtinsearch.org/
 */
@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class DatakickService extends ItemSearchService {
	
	@Inject
	@RestClient
	DataKickLookupClient dataKickLookupClient;
	@Getter
	ExtItemLookupProviderInfo providerInfo;
	
	@Inject
	public DatakickService(
		@RestClient
		DataKickLookupClient dataKickLookupClient,
		@ConfigProperty(name = "productLookup.providers.datakick.enabled", defaultValue = "false")
		boolean enabled
	) {
		this.dataKickLookupClient = dataKickLookupClient;
		this.providerInfo = ExtItemLookupProviderInfo
								.builder()
								.id("datakick")
								.displayName("Datakick")
								.enabled(enabled)
								.description(
									"The open product database, free and open database of products. Mostly for home and food goods. Limited size of database, but free and open to contributions.")
								.acceptsContributions(true)
								.homepage(URI.create("https://gtinsearch.org/"))
								.cost("Free")
								.build();
	}
	
	@Override
	public boolean isEnabled() {
		return this.getProviderInfo().isEnabled();
	}
	
	/**
	 * https://gtinsearch.org/api https://www.gtinsearch.org/api/items/0754523765792
	 *
	 * @param results
	 *
	 * @return
	 */
	@WithSpan
	public Collection<LookupResult> jsonNodeToSearchResults(LookupType type, ArrayNode results) {
		log.debug("Data from Datakick: {}", results);
		
		List<LookupResult> resultsList = new ArrayList<>(results.size());
		
		for (JsonNode curResult : results) {
			ObjectNode curResultJson = (ObjectNode) curResult;
			String brandName = "";
			String name = "";
			Map<String, String> attributes = new HashMap<>();
			
			
			for (Map.Entry<String, JsonNode> curField : curResultJson.properties()) {
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
			
			resultsList.add(ExtItemLookupResult
								.builder()
								.lookupType(type)
								.source(this.getProviderInfo().getDisplayName())
								.name(name)
								.brand(brandName)
								.unifiedName(name)
								.attributes(attributes)
								.build()
			);
		}
		return resultsList;
	}
	
	@Override
	public Optional<Multi<LookupResult>> searchBarcode(String search) {
		return Optional.of(
			this.dataKickLookupClient.getFromUpcCode(search)
				.map(results->this.jsonNodeToSearchResults(LookupType.BARCODE, results))
				.onFailure().recoverWithItem(e -> this.handleErrorRetCollection(LookupType.BARCODE, e))
				
				.onItem().transformToMulti(collection->
											   Multi.createFrom().iterable(collection)
				)
		);
	}
}
