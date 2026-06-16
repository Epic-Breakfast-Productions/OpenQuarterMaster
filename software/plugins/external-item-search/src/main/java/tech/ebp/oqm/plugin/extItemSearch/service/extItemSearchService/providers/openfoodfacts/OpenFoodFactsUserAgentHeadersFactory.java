package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.openfoodfacts;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

@ApplicationScoped
public class OpenFoodFactsUserAgentHeadersFactory implements ClientHeadersFactory {

    private final String userAgent;

    public OpenFoodFactsUserAgentHeadersFactory(
        @ConfigProperty(name = "service.gitLink")
        String appName,
        @ConfigProperty(name = "service.version")
        String appVersion,
        @ConfigProperty(name = "service.email", defaultValue = "openquartermaster.com")
        String contactEmail
    ) {
        this.userAgent = appName + "/" + appVersion + " (" + contactEmail + ")";
    }

    @Override
    public MultivaluedMap<String, String> update(
        MultivaluedMap<String, String> incomingHeaders,
        MultivaluedMap<String, String> clientOutgoingHeaders
    ) {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>(clientOutgoingHeaders);
        headers.add("User-Agent", this.userAgent);
        return headers;
    }
}
