package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory.management;

import io.opentelemetry.instrumentation.annotations.WithSpan;
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
import tech.ebp.oqm.baseStation.service.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.unit.custom.NewCustomUnitRequest;
import tech.ebp.oqm.baseStation.model.units.CustomUnitEntry;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

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
	
	@Inject
	@Getter
	JsonWebToken jwt;
	
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
		@Context SecurityContext securityContext,
		@Valid NewCustomUnitRequest ncur
	) {
		logRequestContext(this.getJwt(), securityContext);
		
		CustomUnitEntry newUnit = ncur.toCustomUnitEntry(this.customUnitService.getNextOrderValue());
		
		UnitUtils.registerAllUnits(newUnit);
		
		ObjectId id = this.customUnitService.add(
			newUnit,
			this.interactingEntityService.getEntity(this.getJwt())
		);
		
		return id;
	}
}
