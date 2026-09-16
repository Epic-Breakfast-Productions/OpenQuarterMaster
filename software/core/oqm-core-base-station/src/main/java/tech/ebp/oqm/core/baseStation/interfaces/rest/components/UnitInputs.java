package tech.ebp.oqm.core.baseStation.interfaces.rest.components;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.dataHelpers.OqmUnitService;

@Slf4j
@Path("/api/pageComponents/unit/inputs")
@Tags({@Tag(name = "Units", description = "Endpoints for getting units.")})
@RolesAllowed(Roles.INVENTORY_VIEW)
@RequestScoped
public class UnitInputs extends ApiProvider {

	@Getter
	@Inject
	OqmUnitService oqmUnitService;

	@Getter
	@Inject
	@Location("tags/inputs/units/unitOptions")
	Template unitCompatibleOptionsTemplate;

	@Getter
	@Inject
	@Location("tags/inputs/units/unitOptionsGroups")
	Template allUnitOptionsTemplate;

	@GET
	@Path("compatibleWith/{unit}")
	@Operation(
		summary = "The set of units compatible with."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the units."
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Response getCompatibleUnits(
		@PathParam("unit") String unit,
		@HeaderParam("Accept") String acceptType
	) {
		log.info("Getting units compatible with {}.", unit);

		ArrayNode compatibleUnits = this.getOqmUnitService().getUnitCompatibleWith(unit);

		return switch (acceptType){
			case MediaType.APPLICATION_JSON -> Response.ok(compatibleUnits).build();
			case MediaType.TEXT_HTML -> Response.ok(getUnitCompatibleOptionsTemplate().data("units", compatibleUnits)).build();
			default -> Response.notAcceptable(Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_HTML_TYPE).build()).build();
		};
	}

	@GET
	@Path("all")
	@Operation(
		summary = "The set of units compatible with."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the units."
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUnits(
		@HeaderParam("Accept") String acceptType
	) {
		log.info("Getting all units.");

		ObjectNode compatibleUnits = this.getOqmUnitService().getAllUnits();

		return switch (acceptType){
			case MediaType.APPLICATION_JSON -> Response.ok(compatibleUnits).build();
			case MediaType.TEXT_HTML -> Response.ok(this.getAllUnitOptionsTemplate().data("allUnitMap", compatibleUnits)).build();
			default -> throw new IllegalStateException("Unexpected value: " + acceptType);
		};
	}
}
