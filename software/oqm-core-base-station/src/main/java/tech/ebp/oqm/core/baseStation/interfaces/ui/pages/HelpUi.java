package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.service.ExternalItemSearchClient;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class HelpUi extends UiProvider {

	@Getter
	@Inject
	@Location("webui/pages/help")
	Template pageTemplate;

	@RestClient
	OqmCoreApiClientService coreApiClient;

	@ConfigProperty(name = "quarkus.rest-client.externalItemSearch.url", defaultValue = "")
	String extSearchUrl;

	@GET
	@Path("help")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Uni<Response> overview() throws MalformedURLException {
		boolean extSearchEnabled = !extSearchUrl.isBlank();
		return this.getUni(
			Map.of(
				"unitCategoryMap", this.coreApiClient.unitGetAll(this.getBearerHeaderStr()),
				"extSearchEnabled", Uni.createFrom().item(extSearchEnabled),
				"allProviderInfo", extSearchEnabled ?
					QuarkusRestClientBuilder.newBuilder()
						.baseUrl(new URL(this.extSearchUrl))
						.build(ExternalItemSearchClient.class).allProviderInfo()
					: Uni.createFrom().nullItem()
			)
		);
	}

}
