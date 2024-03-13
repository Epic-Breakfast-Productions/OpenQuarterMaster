package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.itemList.ItemList;
import tech.ebp.oqm.baseStation.model.object.itemList.ItemListAction;
import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.unit.custom.NewCustomUnitRequest;
import tech.ebp.oqm.baseStation.model.units.CustomUnitEntry;
import tech.ebp.oqm.baseStation.model.units.UnitCategory;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.ItemListSearch;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.ItemListService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

import javax.measure.Unit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/inventory/unit")
@Tags({@Tag(name = "Units", description = "Endpoints for managing Units.")})
@RequestScoped
public class UnitsEndpoints extends EndpointProvider {
	
	@Inject
	CustomUnitService customUnitService;
	
	@GET
	@Operation(
		summary = "Gets the list of supported units."
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
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Map<UnitCategory, Set<Unit<?>>> getUnits() {
		log.info("Getting valid unit list.");
		return UnitUtils.UNIT_CATEGORY_MAP;
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
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Map<Unit<?>, Set<Unit<?>>> getUnitCompatibleMap() {
		log.info("Getting unit set with lists of compatible units.");
		return UnitUtils.UNIT_COMPATIBILITY_MAP;
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
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUnitCompatible(
		@PathParam("unit") String unitString
	) {
		log.info("Getting unit set with lists of compatible units.");
		Unit<?> unit;
		try {
			unit = UnitUtils.unitFromString(unitString);
		} catch(IllegalArgumentException e) {
			//TODO:: determine proper return code
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					   .type(MediaType.TEXT_PLAIN_TYPE)
					   .entity("Invalid Unit String given: \"" + unitString + "\"")
					   .build();
		}
		Set<Unit<?>> units = UnitUtils.UNIT_COMPATIBILITY_MAP.get(unit);
		return Response.ok(units).build();
		
	}
	
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
			this.getInteractingEntity()
		);
		
		return id;
	}
}
