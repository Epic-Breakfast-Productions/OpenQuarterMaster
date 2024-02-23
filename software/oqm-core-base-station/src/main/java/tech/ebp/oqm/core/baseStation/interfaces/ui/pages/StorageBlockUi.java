package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientInfoHealthService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

import java.util.Map;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class StorageBlockUi extends UiProvider {
	
	@Getter
	@Inject
	@Location("webui/pages/storage")
	Template pageTemplate;
	
	@RestClient
	OqmCoreApiClientInfoHealthService coreApiClient;
	
	@GET
	@Blocking
	@Path("storage")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Uni<Response> storagePage(@BeanParam StorageBlockSearch search) {
		this.ensureSearchDefaults(search);
		
		return this.getUni(
			this.setupPageTemplate()
				.data("showSearch", false)
				.data("defaultPageSize", this.getDefaultPageSize()),
			Map.of(
				"searchResults", this.coreApiClient.searchStorageBlocks(this.getBearerHeaderStr(), search)
			)
		);
	}
	
}
