package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
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
import tech.ebp.oqm.core.baseStation.service.modelTweak.SearchResultTweak;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ItemCategorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StoredSearch;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ItemStoredUi extends UiProvider {

	@Getter
	@Inject
	@Location("webui/pages/itemsStored")
	Template pageTemplate;

	@RestClient
	OqmCoreApiClientService coreApiClient;

	@Getter(onMethod = @__(@Override))
	@ConfigProperty(name="ui.itemsStored.search.defaultPageSize")
	int defaultPageSize;

	@Inject
	SearchResultTweak searchResultTweak;

	@GET
	@Path("itemsStored")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Uni<Response> itemsPage(@BeanParam StoredSearch search) {
		this.ensureSearchDefaults(search);

		return this.getUni(
			this.setupPageTemplate()
				.data("showSearch", false),
			Map.of(
				"allCategorySearchResults", this.coreApiClient.itemCatSearch(this.getBearerHeaderStr(), this.getSelectedDb(), new ItemCategorySearch()),
				"searchResults", this.coreApiClient.invItemStoredSearch(this.getBearerHeaderStr(), this.getSelectedDb(), search)
									 .call(results->searchResultTweak.addStorageBlockLabelToSearchResult(results, this.getSelectedDb(), this.getBearerHeaderStr(), "state", "storageBlock"))
									 .call(results->searchResultTweak.addItemDetailsToSearchResult(results, this.getSelectedDb(), "item", this.getBearerHeaderStr())),
				"allUnitMap", this.coreApiClient.unitGetAll(this.getBearerHeaderStr())
			)
		);
	}

}
