package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/identifier/general")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class GeneralIdPassthrough extends PassthroughProvider {
	
	
	@Getter
	@Inject
	@Location("tags/inputs/identifiers/addedGeneralIdentifier.qute.html")
	Template newIdentifierTemplate;
	
	@GET
	@Path("getIdObject/{identifier}")
	@Operation(
		summary = "Gets the identifier object derived from the identifier string."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the code object."
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> getIdObject(
		@PathParam("identifier") String code,
		@HeaderParam("Accept") String acceptType
	) {
		return this.getOqmCoreApiClient().generalIdGetObj(this.getBearerHeaderStr(), code)
				   .map((ObjectNode generalIdentifier)->{
					   log.debug("Got general identifier: {}", generalIdentifier);
					   //noinspection SwitchStatementWithTooFewBranches
					   return switch (acceptType) {
						   case MediaType.TEXT_HTML -> Response.ok(
							   newIdentifierTemplate
								   .data("rootPrefix", this.getRootPrefix())
								   .data("generalId", generalIdentifier)
						   ).build();
						   default -> Response.ok(generalIdentifier).build();
					   };
				   });
	}
	
	@GET
	@Path("barcode/{type}/{value}")
	@Operation(
		summary = "A barcode that represents the string given."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@Produces("image/svg+xml")
	public Uni<Response> getBarcode(
		@PathParam("type") String type,
		@PathParam("value") String data
	) {
		return this.getOqmCoreApiClient()
				   .generalIdGetBarcodeImage(this.getBearerHeaderStr(), type, data)
				   .map((String xmlData)->{
					   return Response.status(Response.Status.OK)
								  .entity(xmlData)
								  .header("Content-Disposition", "attachment;filename=" + type + "_"+data+".svg")
								  .type("image/svg+xml")
								  .build();
				   });
	}
}
