package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.openfoodfacts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
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
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.ResultMappingUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class OpenFoodFactsService extends ItemSearchService {

    private final OpenFoodFactsLookupClient openFoodFactsLookupClient;
    private final OpenFoodFactsSearchClient openFoodFactsSearchClient;
    private final int responseSize;

    public OpenFoodFactsService(
        @RestClient OpenFoodFactsLookupClient openFoodFactsLookupClient,
        @RestClient OpenFoodFactsSearchClient openFoodFactsSearchClient,
        @ConfigProperty(name = "productLookup.providers.openfoodfacts.enabled", defaultValue = "true") boolean enabled,
        @ConfigProperty(name = "productLookup.providers.openfoodfacts.responseSize", defaultValue = "10") int responseSize) {
        super(
            enabled,
            LookupService.OPENFOODFACTS,
            ExtItemLookupProviderInfo.
                builder()
                .displayName("Open Food Facts")
                .description("Open database of food products")
                .cost("free")
                .acceptsContributions(true)
                .homepage(URI.create("https://openfoodfacts.github.io")));
        this.openFoodFactsLookupClient = openFoodFactsLookupClient;
        this.openFoodFactsSearchClient = openFoodFactsSearchClient;
        this.responseSize = responseSize;
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

    @Override
    protected Multi<LookupResult> performSearch(LookupSource source, LookupMethod lookupMethod, String term) {
        if(source != LookupSource.OPENFOODFACTS) {
            return Multi.createFrom().empty();
        }
        return switch (source) {
            case OPENFOODFACTS ->
                switch (lookupMethod) {
                    case BARCODE -> this.openFoodFactsLookupClient.getProduct(term)
                        .map(result -> this.partJsonToResult(source, lookupMethod, result))
                        .onFailure().recoverWithItem(e -> this.handleError(source, lookupMethod, e))
                        .toMulti();
                    case TEXT -> this.openFoodFactsSearchClient.search(responseSize, term)
                        .map(result -> this.searchJsonToResults(source, lookupMethod, result))
                        .onFailure().recoverWithItem(e -> this.handleErrorRetCollection(source, lookupMethod, e))
                        .onItem().transformToMulti(collection -> Multi.createFrom().iterable(collection));
                    default -> throw new IllegalArgumentException("Invalid lookup method: " + lookupMethod);
                };
            default -> throw new IllegalArgumentException("Invalid lookup source: " + source);
        };
    }

    private LookupResult partJsonToResult(LookupSource source, LookupMethod method, ObjectNode results) {
        log.debug("Data from OpenFoodFacts: {}", results.toPrettyString());
        ExtItemLookupResult.Builder<?, ?> resultBuilder = this.setupResponseBuilder(ExtItemLookupResult.builder(), source, method);

        if (results.get("product") != null && results.get("product").isObject()) {
            results = (ObjectNode) results.get("product");
        }

        List<String> images = new ArrayList<>();
        Map<String, String> links = new HashMap<>();
        Map<String, String> identifiers = new HashMap<>();
        Map<String, String> attributes = new HashMap<>();
        String description = "";
        String name = "";

        for (Map.Entry<String, JsonNode> currentMap : results.properties()) {
            String currentKey = currentMap.getKey();
            JsonNode currentVal = currentMap.getValue();

            if (ResultMappingUtils.isFieldEmpty(currentVal)) {
                continue;
            }

            switch (currentKey) {
                case "product_name" -> {
                    name = currentVal.asText();
                    resultBuilder.name(name);
                    resultBuilder.unifiedName(name);
                }
                case "generic_name", "ingredients_text" -> {
                    if (description.isBlank()) {
                        description = currentVal.asText();
                    } else {
                        attributes.put(currentKey, currentVal.asText());
                    }
                }
                case "code", "_id" -> identifiers.put(currentKey, currentVal.asText());
                case "url", "link" -> links.put("openfoodfacts", currentVal.asText());
                case "image_url",
                     "image_small_url",
                     "image_thumb_url",
                     "image_front_url",
                     "image_front_small_url",
                     "image_front_thumb_url",
                     "image_ingredients_url",
                     "image_ingredients_small_url",
                     "image_ingredients_thumb_url",
                     "image_nutrition_url",
                     "image_nutrition_small_url",
                     "image_nutrition_thumb_url",
                     "image_packaging_url",
                     "image_packaging_small_url",
                     "image_packaging_thumb_url" -> images.add(currentVal.asText());
                case "images", "selected_images" -> collectImageUrls(currentVal, images);
                case "brands",
                     "categories",
                     "countries",
                     "origins",
                     "stores",
                     "quantity",
                     "product_quantity",
                     "product_type",
                     "packaging_text",
                     "nova_group",
                     "nutrition_grade_fr",
                     "nutrition_grades" -> attributes.put(currentKey, currentVal.asText());
                default -> putAttribute(attributes, currentKey, currentVal);
            }

        }

        if (description.isBlank()) {
            description = firstNonBlank(
                name,
                identifiers.get("code"),
                identifiers.get("_id")
            );
        }

        if (name.isBlank()) {
            name = firstNonBlank(
                description,
                identifiers.get("code"),
                identifiers.get("_id")
            );
        }

        if (name.isBlank()) {
            name = "OpenFoodFacts item";
        }

        if (description.isBlank()) {
            description = name;
        }

        resultBuilder
            .name(name)
            .unifiedName(firstNonBlank(name, description, identifiers.get("code"), identifiers.get("_id")))
            .description(description)
            .identifiers(identifiers)
            .links(links)
            .images(images)
            .attributes(attributes);

        return resultBuilder.build();
    }

    private Collection<LookupResult> searchJsonToResults(LookupSource source, LookupMethod method, ObjectNode results) {
        log.debug("Search data from OpenFoodFacts: {}", results.toPrettyString());

        JsonNode productsNode = firstArrayNode(results, "hits", "products");
        if (ResultMappingUtils.isFieldEmpty(productsNode)) {
            return List.of(
                this.setupResponseBuilder(LookupResultNoResults.builder(), source, method)
                    .detail("No items found.")
                    .build()
            );
        }

        List<LookupResult> output = new ArrayList<>();
        for (JsonNode product : productsNode) {
            if (product != null && product.isObject()) {
                output.add(this.partJsonToResult(source, method, (ObjectNode) product));
            }
        }

        if (output.isEmpty()) {
            return List.of(
                this.setupResponseBuilder(LookupResultNoResults.builder(), source, method)
                    .detail("No items found.")
                    .build()
            );
        }

        return output;
    }

    private static JsonNode firstArrayNode(ObjectNode results, String... fields) {
        for (String field : fields) {
            JsonNode node = results.get(field);
            if (node != null && node.isArray()) {
                return node;
            }
        }
        return null;
    }

    private static void putAttribute(Map<String, String> attributes, String key, JsonNode value) {
        if (value == null || value.isNull()) {
            return;
        }

        if (value.isTextual() || value.isNumber() || value.isBoolean()) {
            attributes.put(key, value.asText());
            return;
        }

        attributes.put(key, value.toString());
    }

    private static void collectImageUrls(JsonNode node, List<String> images) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isTextual()) {
            String url = node.asText();
            if (!url.isBlank() && url.startsWith("http")) {
                images.add(url);
            }
            return;
        }

        if (node.isArray()) {
            for (JsonNode curNode : node) {
                collectImageUrls(curNode, images);
            }
            return;
        }

        if (node.isObject()) {
            for (Iterator<JsonNode> iter = node.elements(); iter.hasNext(); ) {
                collectImageUrls(iter.next(), images);
            }
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
