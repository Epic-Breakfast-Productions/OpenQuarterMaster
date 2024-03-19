package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ItemCategorySearch;

import java.util.Map;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ItemCategoriesUi extends UiProvider {
	
	@Getter
	@Inject
	@Location("webui/pages/categories")
	Template pageTemplate;
	
	@RestClient
	OqmCoreApiClientService coreApiClient;
	
	@Getter(onMethod = @__(@Override))
	@ConfigProperty(name="ui.itemCategory.search.defaultPageSize")
	int defaultPageSize;
	
	@GET
	@Path("itemCategories")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Uni<Response> storagePage(@BeanParam ItemCategorySearch search) {
		this.ensureSearchDefaults(search);
		
		return this.getUni(
			this.setupPageTemplate()
				.data("showSearch", false),
			Map.of(
				"allCategorySearchResults", this.coreApiClient.itemCatSearch(this.getBearerHeaderStr(), new ItemCategorySearch()),
				"searchResults", this.coreApiClient.itemCatSearch(this.getBearerHeaderStr(), search).call((ObjectNode results)->{
					return addParentLabelsToSearchResults(results, "name", this.coreApiClient::itemCatGet);
				})
			)
		);
	}
	
}
