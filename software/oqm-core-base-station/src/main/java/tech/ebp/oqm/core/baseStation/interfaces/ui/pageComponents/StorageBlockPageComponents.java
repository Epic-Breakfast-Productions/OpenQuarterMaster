package tech.ebp.oqm.core.baseStation.interfaces.ui.pageComponents;


import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

import static tech.ebp.oqm.core.baseStation.interfaces.ui.pageComponents.PageComponentProvider.PAGE_COMPONENT_ROOT;

@Slf4j
@Path(PAGE_COMPONENT_ROOT + "/storageBlock")
@Tags({@Tag(name = "UI")})
@Produces(MediaType.TEXT_HTML)
@RequestScoped
public class StorageBlockPageComponents extends PageComponentProvider {
	
	@Inject
	@Location("webui/pages/storage")
	Template pageTemplate;
	
	@RestClient
	OqmCoreApiClientService coreApiClient;
	
	@GET
	@Path("treeItem")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response overview() {
		JsonNode itemStats = this.coreApiClient.invItemCollectionStats(this.getBearerHeaderStr()).await().indefinitely();
		
		log.debug("Item stats json: {}", itemStats);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			"<p>uwu</p>"
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}

}
