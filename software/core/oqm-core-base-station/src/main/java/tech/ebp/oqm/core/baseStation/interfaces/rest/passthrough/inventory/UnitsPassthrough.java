package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.PassthroughProvider;

import java.util.Map;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/inventory/unit")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class UnitsPassthrough extends PassthroughProvider {
	
	@Getter
	@Inject
	@Location("tags/inputs/units/unitOptions.html")
	Template unitOptionsTemplate;
	
	@GET
	@Operation(
		summary = "Gets the list of supported units."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the list.",
		content = @Content(
			mediaType = "application/json"
		)
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> getUnits() {
		return this.handleCall(
			this.getOqmCoreApiClient().unitGetAll(this.getBearerHeaderStr())
		);
	}
	
	@GET
	@Path("compatibility")
	@Operation(
		summary = "Gets the set of compatible units."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the list.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				//TODO: better
				implementation = Map.class
			)
		)
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> getUnitCompatibleMap() {
		return this.handleCall(
			this.getOqmCoreApiClient().unitGetCompatibleMap(this.getBearerHeaderStr())
		);
	}
	
	@GET
	@Path("compatibility/{unit}")
	@Operation(
		summary = "Gets the compatible units of the unit given"
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the list.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				//TODO: better
				implementation = Map.class
			)
		)
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> getUnitCompatible(
		@PathParam("unit") String unitString,
		@HeaderParam("Accept") String acceptType
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().unitGetUnitCompatibleWith(this.getBearerHeaderStr(), unitString)
				.map((ArrayNode results)->{
					if (acceptType.equals("text/html")) {
						return Response.ok(unitOptionsTemplate.data("units", results), MediaType.TEXT_HTML).build();
					}
					return Response.ok(results).build();
				})
		);
	}
	
	@POST
	@Path("custom")
	@Operation(
		summary = "Adds a new custom unit."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = "application/json"
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> createCustomUnit(
		ObjectNode ncur
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().unitCreateCustomUnit(this.getBearerHeaderStr(), ncur)
		);
	}
	
	@PUT
	@Path("convert")
	@Operation(
		summary = "Converts quantit(ies)."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = "application/json"
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> convert(
		JsonNode convertRequest
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().unitConvertQuantity(convertRequest)
		);
	}
}
