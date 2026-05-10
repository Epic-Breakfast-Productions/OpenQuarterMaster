package tech.ebp.oqm.core.api.interfaces.endpoints.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.unit.custom.NewCustomUnitRequest;
import tech.ebp.oqm.core.api.model.rest.unit.custom.NewDerivedCustomUnitRequest;
import tech.ebp.oqm.core.api.model.units.ConvertRequest;
import tech.ebp.oqm.core.api.model.units.CustomUnitEntry;
import tech.ebp.oqm.core.api.model.units.UnitCategory;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.model.units.ValidUnitDimension;
import tech.ebp.oqm.core.api.model.rest.search.CustomUnitSearch;
import tech.ebp.oqm.core.api.service.mongo.CustomUnitService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/unit")
@Tags({@Tag(name = "Units", description = "Endpoints for managing Units.")})
@RequestScoped
public class UnitsEndpoints extends EndpointProvider {
	
	@Inject
	CustomUnitService customUnitService;
	
	@Inject
	ObjectMapper objectMapper;
	
	@GET
	@Operation(
		summary = "Gets the map of supported units."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the list.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				type = SchemaType.OBJECT,
				properties = @SchemaProperty(
					name = "unit category",
					type = SchemaType.ARRAY,
					description = "List of supported units for each category.",
					contentSchema = Unit.class,
												examples = {"[{\"string\": \"units\"}]"}
				)
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Map<UnitCategory, Set<Unit<?>>> getUnits() {
		log.debug("Getting valid unit list.");
		return UnitUtils.UNIT_CATEGORY_MAP;
	}
	
	@GET
	@Path("dimensions")
	@Operation(
		summary = "Gets the list of supported unit dimensions."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the list."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public ValidUnitDimension[] getUnitDimensions() {
		return ValidUnitDimension.values();
	}
	
	@GET
	@Path("deriveTypes")
	@Operation(
		summary = "Gets the ways that can be used to derive a custom unit from another one.."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the list."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public NewDerivedCustomUnitRequest.DeriveType[] getUnitDeriveTypes() {
		return NewDerivedCustomUnitRequest.DeriveType.values();
	}
	
	@GET
	@Path("compatibility")
	@Operation(
		summary = "Gets the map of which units are compatible with other units."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the list.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				type = SchemaType.OBJECT,
				properties = @SchemaProperty(
					name = "unit",
					type = SchemaType.ARRAY,
					description = "List of units that are compatible with this unit.",
					contentSchema = Unit.class,
					examples = {"[{\"string\": \"units\"}]"}
				)
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Map<Unit<?>, Set<Unit<?>>> getUnitCompatibleMap() {
		return UnitUtils.UNIT_COMPATIBILITY_MAP;
	}
	
	@GET
	@Path("compatibility/{unit}")
	@Operation(
		summary = "Gets the units that are compatible with the the unit given"
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the list."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Unit<?>> getUnitCompatible(
		@PathParam("unit") String unitString
	) {
		log.debug("Getting unit set with lists of compatible units.");
		Unit<?> unit;
		try {
			unit = UnitUtils.unitFromString(unitString);
		} catch(IllegalArgumentException e) {
			//TODO:: determine proper error
			throw new ValidationException("Invalid unit given: " + unitString, e);
		}
		return  UnitUtils.UNIT_COMPATIBILITY_MAP.get(unit);
	}
	
	@POST
	@Path("custom")
	@Operation(
		summary = "Adds a new custom unit."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added."
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
		return this.customUnitService.add(null, ncur);
	}
	
	@GET
	@Path("custom")
	@Operation(
		summary = "Gets all custom units."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added."
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Produces(MediaType.APPLICATION_JSON)
	public SearchResult<CustomUnitEntry> getCustomUnits(
		@BeanParam CustomUnitSearch search
	) {
		return this.customUnitService.search(search);
	}
	
	private static Quantity<?> convert(@Valid ConvertRequest request) {
		//noinspection unchecked
		return request.getQuantity().to(request.getNewUnit());
	}
	
	@PUT
	@Path("convert")
	@Operation(
		summary = "Converts a quantity to a different unit. Supply either a single ConvertRequest or an array of them. Returns just the object if given an object, array if given"
				  + " array."
	)
	@APIResponse(
		responseCode = "200",
		description = "Quantity converted.",
		content = @Content(mediaType = "application/json", schema = @Schema(implementation = Quantity.class))
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response convert(
		@Schema(implementation = ConvertRequest.class)
		JsonNode convertRequestJson
	) {
		//TODO:: wrap in a good exception for invalid conversions?
		Object output = null;
		if (convertRequestJson.isObject()) {
			ConvertRequest convertRequest = this.objectMapper.convertValue(convertRequestJson, ConvertRequest.class);
			output = convert(convertRequest);
		} else if (convertRequestJson.isArray()) {
			List<Quantity<?>> out = new ArrayList<>(convertRequestJson.size());
			//TODO:: contemplate if OBJECT_MAPPER.readValue(response.extract().body().asString(), new TypeReference<List<Quantities>>() { }) is better
			for (JsonNode curConvertRequestJson : (ArrayNode) convertRequestJson) {
				ConvertRequest convertRequest = this.objectMapper.convertValue(curConvertRequestJson, ConvertRequest.class);
				out.add(convert(convertRequest));
			}
			output = out;
		}
		return Response.ok(output).build();
	}
	
	
}
