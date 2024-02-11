package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

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
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientInfoHealthService;

//@Blocking
//@Slf4j
//@Path("/")
//@Tags({@Tag(name = "UI")})
//@RequestScoped
//@Produces(MediaType.TEXT_HTML)
//public class StorageBlockUi extends UiProvider {
//
//	@Inject
//	@Location("webui/pages/storage")
//	Template pageTemplate;
//
//	@RestClient
//	OqmCoreApiClientInfoHealthService coreApiClient;
//
//	@GET
//	@Blocking
//	@Path("storage")
//	@RolesAllowed(Roles.INVENTORY_VIEW)
//	public Response overview() {
//		JsonNode itemStats = this.coreApiClient.getItemStats(this.getBearerHeaderStr()).await().indefinitely();
//
//		log.debug("Item stats json: {}", itemStats);
//
//		Response.ResponseBuilder responseBuilder = Response.ok(
//			this.setupPageTemplate(pageTemplate)
//				.data("numItems", itemStats.get("size").asLong())
//				.data("totalExpired", itemStats.get("numExpired").asLong())
//			,
//			MediaType.TEXT_HTML_TYPE
//		);
//
//		return responseBuilder.build();
//	}
//
//}
