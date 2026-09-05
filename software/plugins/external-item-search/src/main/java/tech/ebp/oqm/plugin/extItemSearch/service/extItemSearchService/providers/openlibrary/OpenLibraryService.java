package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.openlibrary;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResultNoResults;

@ApplicationScoped
public class OpenLibraryService extends ItemSearchService {

    private final OpenLibraryLookupClient openLibraryLookupClient;
    private final int responseSize;

    public OpenLibraryService(
        @RestClient OpenLibraryLookupClient openLibraryLookupClient,
        @ConfigProperty(name = "productLookup.providers.openlibrary.enabled", defaultValue = "true") boolean enabled,
        @ConfigProperty(name = "productLookup.providers.openlibrary.responseSize", defaultValue = "10") int responseSize) {
        super(
            enabled,
            LookupService.OPENLIBRARY,
            ExtItemLookupProviderInfo.
                builder()
                .displayName("Open Library")
                .description("open and editable library catalog")
                .cost("free")
                .acceptsContributions(true)
                .homepage(URI.create("https://openlibrary.org")));
        this.openLibraryLookupClient = openLibraryLookupClient;
        this.responseSize = responseSize;
    }

    @Override
    protected Multi<LookupResult> performSearch(LookupSource source, LookupMethod lookupMethod, String term) {
        return switch (source){
            case OPENLIBRARY ->
                switch (lookupMethod) {
                    case BARCODE -> this.openLibraryLookupClient.search(term)
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

    private Collection<LookupResult> mapJsonToResponse(LookupSource source, LookupMethod method, ObjectNode results) {
        if (results.get("docs") == null || results.get("docs").isEmpty()) {
            return List.of(
                this.setupResponseBuilder(LookupResultNoResults.builder(), source, method)
                    .detail("No items found.")
                    .build()
            );
        }

        List<LookupResult> response = new ArrayList<>();
        Iterator<JsonNode> docs = results.get("docs").elements();
        int count = 0;
        while (docs.hasNext() && count < this.responseSize) {
            JsonNode doc = docs.next();
            count++;

            if (doc == null || !doc.isObject()) {
                continue;
            }

            ExtItemLookupResult.Builder<?, ?> builder = this.setupResponseBuilder(ExtItemLookupResult.builder(), source, method);

            String title = doc.has("title") && !doc.get("title").isNull() ? doc.get("title").asText() : "name is not specified";

            Map<String, String> identifiers = new HashMap<>();
            Map<String, String> attributes = new HashMap<>();
            Map<String, String> links = new HashMap<>();
            List<String> images = new ArrayList<>();

            if (doc.has("first_publish_year") && !doc.get("first_publish_year").isNull()) {
                attributes.put("first_publish_year", doc.get("first_publish_year").asText());
            }

            if (doc.has("edition_count") && !doc.get("edition_count").isNull()) {
                attributes.put("edition_count", doc.get("edition_count").asText());
            }

            builder
                .name(title)
                .unifiedName(title == null || title.isBlank() ? "OpenLibrary item" : title)
                .description(title)
                .identifiers(identifiers)
                .attributes(attributes)
                .links(links)
                .images(images);

            response.add(builder.build());
        }

        return response;
    }
}
