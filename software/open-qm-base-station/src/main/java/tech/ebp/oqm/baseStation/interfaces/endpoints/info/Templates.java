package tech.ebp.oqm.baseStation.interfaces.endpoints.info;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.service.importExport.csv.InvItemCsvConverter;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.io.FileNotFoundException;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/templates/")
@Tags({@Tag(name = "Templates", description = "Endpoints to get templates for users to use.")})
@RequestScoped
public class Templates extends EndpointProvider {
	
	@Inject
	@Location("templates/items.csv")
	Template itemsCsv;
	
	@GET
	@Path("itemsCsv")
	@Operation(
		summary = "Gets a template for entering items by CSV."
	)
	@APIResponse(
		responseCode = "200",
//		description = "",
		content = @Content(mediaType = "text/csv")
	)
	@PermitAll
	@Produces({"text/csv", "text/plain"})
	public Response getItemCsvTemplate() {
		return Response.ok(
			itemsCsv.instance()
		).build();
	}
	
}
