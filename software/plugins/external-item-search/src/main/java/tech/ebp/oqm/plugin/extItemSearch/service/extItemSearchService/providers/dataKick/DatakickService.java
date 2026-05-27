package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.dataKick;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResultNoResults;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource;

import java.net.URI;
import java.util.*;

/**
 * - https://gtinsearch.org/
 */
@ApplicationScoped
@Slf4j
public class DatakickService extends ItemSearchService {
	
	private DataKickLookupClient dataKickLookupClient;
	
	@Inject
	public DatakickService(
		@RestClient
		DataKickLookupClient dataKickLookupClient,
		@ConfigProperty(name = "productLookup.providers.datakick.enabled", defaultValue = "false")
		boolean enabled
	) {
		this.dataKickLookupClient = dataKickLookupClient;
		super(
			enabled,
			LookupService.DATAKICK,
			ExtItemLookupProviderInfo
				.builder()
				.displayName("Datakick")
				.description(
					"The open product database, free and open database of products. Mostly for home and food goods. Limited size of database, but free and open to contributions.")
				.acceptsContributions(true)
				.homepage(URI.create("https://gtinsearch.org/"))
				.cost("Free")
		);
	}
	
	/**
	 * https://gtinsearch.org/api https://www.gtinsearch.org/api/items/0754523765792
	 *
	 * @param results
	 *
	 * @return
	 */
	public Collection<LookupResult> jsonNodeToSearchResults(LookupSource source, LookupMethod method, ArrayNode results) {
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
			
			resultsList.add(
				this.setupResponseBuilder(ExtItemLookupResult.builder(), source, method)
					.name(name)
					.unifiedName(name)
					.attributes(attributes)
					.build()
			);
		}
		return resultsList;
	}
	
	@Override
	protected Multi<LookupResult> performSearch(LookupSource source, LookupMethod method, String term) {
		return switch (source) {
			case DATAKICK ->
				switch (method) {
					case BARCODE -> this.dataKickLookupClient.getFromUpcCode(term)
										 .map(result->this.jsonNodeToSearchResults(source, method, result))
										 .onFailure().recoverWithItem(e->this.handleErrorRetCollection(source, method, e))
										.onItem().transformToMulti(collection-> Multi.createFrom().iterable(collection));
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
					.detail("No items found.")
					.build()
			);
		}
		return Optional.empty();
	}
}
