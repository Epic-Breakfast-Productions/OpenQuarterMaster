package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.core.baseStation.utils.Searches;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientInfoHealthService;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class OverviewUi extends UiProvider {
	
	@Getter
	@Inject
	@Location("webui/pages/overview")
	Template pageTemplate;
	
	@RestClient
	OqmCoreApiClientInfoHealthService coreApiClient;
	
	@Inject
	ExecutorService executorService;
	
	@GET
	@Path("overview")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Uni<Response> overview() {
		return this.getUni(
			Map.of(
				"itemCollectionStats", this.coreApiClient.getItemStats(this.getBearerHeaderStr()),
				"storageCollectionStats", this.coreApiClient.getStorageBlockStats(this.getBearerHeaderStr()),
				"totalExpired", this.coreApiClient.getStorageBlockStats(this.getBearerHeaderStr()).map(stats -> stats.get("size").asLong()),
				"parentBlocks", this.coreApiClient.searchStorageBlocks(this.getBearerHeaderStr(), Searches.PARENT_SEARCH)
			)
		);
	}
	
}
