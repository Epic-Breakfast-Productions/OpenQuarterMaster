package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.isbndb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.ResultMappingUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
public class ISBNdbLookupService extends ItemSearchService {

    private final String apiKey;
    private final ISBNdbLookupClient isbndbLookupClient;
    private final ObjectMapper objectMapper;

    @Inject
    public ISBNdbLookupService(
        @RestClient
        ISBNdbLookupClient isbndbLookupClient,
        @ConfigProperty(name = "productLookup.providers.isbndb.enabled", defaultValue = "false")
        boolean enabled,
        @ConfigProperty(name = "productLookup.providers.isbndb.apiKey", defaultValue = "")
        String apiKey,
        ObjectMapper objectMapper) {
        super(
            enabled,
            LookupService.ISBNDB,
            ExtItemLookupProviderInfo
                .builder()
                .displayName("ISBNdb")
                .description("The World's largest book database.")
                .acceptsContributions(false)
                .homepage(URI.create("https://isbndb.com/"))
                .cost("Paid")
        );

        if(apiKey == null || apiKey.isBlank()) {
            log.warn("API key for ISBNDB was null or blank.");
            this.apiKey = null;
        } else {
            this.apiKey = apiKey;
        }

        this.isbndbLookupClient = isbndbLookupClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && this.apiKey != null && !this.apiKey.isBlank();
    }

    @Override
    protected Multi<LookupResult> performSearch(LookupSource source, LookupMethod lookupMethod, String term) {
        return switch (source){
            case ISBNDB ->
                switch (lookupMethod) {
                    case BARCODE -> this.isbndbLookupClient.searchBarcode(apiKey,  term)
                        .map(result -> mapJsonToResponse(source, lookupMethod, result))
                        .onFailure().recoverWithItem(e -> this.handleErrorRetCollection(source, lookupMethod, e))
                        .onItem().transformToMulti(collection -> Multi.createFrom().iterable(collection));
                    //TODO: #1338
                    case TEXT -> throw new IllegalArgumentException("Text lookup method search is not implemented yet");
                    default -> throw new IllegalArgumentException("Invalid lookup method: " + lookupMethod);
                };
            default -> throw new IllegalArgumentException("Invalid lookup source: " + source);
        };
    }

    private Collection<LookupResult> mapJsonToResponse(LookupSource source, LookupMethod method, JsonNode results) {
        log.debug("Data from ISBNdb: {}", results.toPrettyString());
        ExtItemLookupResult.Builder<?, ?> resultBuilder = this.setupResponseBuilder(ExtItemLookupResult.builder(), source, method);
        List<LookupResult> resultList = new ArrayList<>(results.size());
        if (results.get("book") != null && results.get("book").isObject()) {
            results = results.get("book");
        }

        List<String> images = new ArrayList<>();
        Map<String, String> links = new HashMap<>();
        Map<String, String> identifiers = new HashMap<>();
        Map<String, String> attributes = new HashMap<>();
        Map<String, String> prices = new HashMap<>();
        String description = "";
        String name = "";

        for (Map.Entry<String, JsonNode> currentMap : results.properties()) {
            String key = currentMap.getKey();
            JsonNode value = currentMap.getValue();

            if (ResultMappingUtils.isFieldEmpty(value)) {
                continue;
            }

            switch (key) {
                case "title" -> name = value.asText();
                case "isbn10", "isbn13" -> identifiers.put(key, value.asText());
                case "publisher", "language", "date_published" -> attributes.put(key, value.asText());
                case "image", "image_original" -> images.add(value.asText());
                case "excerpt" -> description = value.asText();
                case "authors", "subjects" ->
                    attributes.put(key,
                        String.join(", ", this.fillList((ArrayNode) value, String.class))
                    );
                case "prices" -> {
                    for (JsonNode item : value) {
                        String merchant = item.path("merchant").asText();
                        String price = item.path("price").asText();

                        if (!merchant.isBlank() && !price.isBlank()) {
                            prices.put(merchant, price);
                        }
                    }
                }
                default -> attributes.put(key, value.isValueNode() ? value.asText() : value.toString());
            }
        }
        resultList.add(
            resultBuilder
                .name(name)
                .unifiedName(name)
                .description(description)
                .identifiers(identifiers)
                .links(links)
                .images(images)
                .prices(prices)
                .attributes(attributes)
                .build()
        );
        return resultList;
    }

    private <T> List<T> fillList(ArrayNode array, Class<T> type) {
        List<T> result = new ArrayList<>();

        for (JsonNode item : array) {
            result.add(objectMapper.convertValue(item, type));
        }

        return result;
    }
}
