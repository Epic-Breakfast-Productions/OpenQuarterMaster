package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
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
import tech.ebp.oqm.core.baseStation.service.modelTweak.SearchResultTweak;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ItemCategorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ItemCheckoutSearch;

import java.util.Map;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ItemCheckoutUi extends UiProvider {
	
	@Getter
	@Inject
	@Location("webui/pages/itemCheckout")
	Template pageTemplate;
	
	@RestClient
	OqmCoreApiClientService coreApiClient;
	
	@Getter(onMethod = @__(@Override))
	@ConfigProperty(name="ui.itemCheckout.search.defaultPageSize")
	int defaultPageSize;

	@Inject
	SearchResultTweak searchResultTweak;
	
	@GET
	@Path("itemCheckout")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Uni<Response> itemCheckoutPage(@BeanParam ItemCheckoutSearch search) {
		this.ensureSearchDefaults(search);
		
		return this.getUni(
			this.setupPageTemplate()
				.data("showSearch", false),
			Map.of(
				"allCategorySearchResults", this.coreApiClient.itemCatSearch(this.getBearerHeaderStr(), this.getSelectedDb(), new ItemCategorySearch()),
				"searchResults", this.coreApiClient.itemCheckoutSearch(this.getBearerHeaderStr(), this.getSelectedDb(), search)
					.call(results -> searchResultTweak.addStorageBlockLabelToSearchResult(results, this.getSelectedDb(), "fromBlock", this.getBearerHeaderStr()))
					.call(results -> searchResultTweak.addItemNameToSearchResult(results, this.getSelectedDb(), "item", this.getBearerHeaderStr()))
					.call(results -> searchResultTweak.addCreatedByInteractingEntityRefToCheckoutSearchResult(results, this.getSelectedDb(), this.getBearerHeaderStr()))
					.call(results -> searchResultTweak.addInteractingEntityRefToCheckoutSearchResult(results, this.getBearerHeaderStr()))
			)
		);
	}
	
}
