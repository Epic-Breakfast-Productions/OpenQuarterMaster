package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory.management;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.unit.custom.NewCustomUnitRequest;
import tech.ebp.oqm.baseStation.model.units.CustomUnitEntry;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

/**
 * TODO:: make this main object provider?
 */
@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/inventory/manage/customUnit")
@Tags({@Tag(name = "Inventory Management", description = "Endpoints for inventory management.")})
@RequestScoped
public class CustomUnit extends EndpointProvider {
	
	@Inject
	CustomUnitService customUnitService;
	
	@Inject
	InteractingEntityService interactingEntityService;
	
	@POST
	@Operation(
		summary = "Adds a new custom unit."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ObjectId.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectId createCustomUnit(
		@Valid NewCustomUnitRequest ncur
	) {
		CustomUnitEntry newUnit = ncur.toCustomUnitEntry(this.customUnitService.getNextOrderValue());
		
		UnitUtils.registerAllUnits(newUnit);
		
		ObjectId id = this.customUnitService.add(
			newUnit,
			this.interactingEntityService.getEntity(this.getJwt())
		);
		
		return id;
	}
}
