package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.openfoodfacts;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResultNoResults;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource;

import java.net.URI;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class OpenFoodFactsService extends ItemSearchService {

    private OpenFoodFactsLookupClient openFoodFactsLookupClient;
    private int RESPONSE_SIZE = 1; //move it to application.yml?

    public OpenFoodFactsService(
        @RestClient OpenFoodFactsLookupClient openFoodFactsLookupClient, //TODO
        @ConfigProperty(name = "productLookup.providers.openfoodfacts.enabled", defaultValue = "false") boolean enabled) {
        super(
            enabled,
            LookupService.OPENFOODFACTS,
            ExtItemLookupProviderInfo.
                builder()
                .displayName("openfoodfacts")
                .description("Open database of food products")
                .cost("free")
                .acceptsContributions(true)
                .homepage(URI.create("https://openfoodfacts.github.io")));
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
        return switch (source){
            case OPENFOODFACTS ->
                switch (lookupMethod) {
                    case BARCODE -> this.openFoodFactsLookupClient.getProduct(term);
                    case TEXT -> this.openFoodFactsLookupClient.search(term, RESPONSE_SIZE);
                    default -> throw new IllegalArgumentException("Invalid lookup method: " + lookupMethod);
                };
            default -> throw new IllegalArgumentException("Invalid lookup source: " + source);
        };
    }
}
