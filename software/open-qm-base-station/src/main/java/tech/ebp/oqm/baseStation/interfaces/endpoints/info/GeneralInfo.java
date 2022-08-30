package tech.ebp.oqm.baseStation.interfaces.endpoints.info;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.lib.core.UnitUtils;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.measure.Unit;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Variant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Traced
@Slf4j
@Path("/api/info")
@Tags({@Tag(name = "Informational", description = "Endpoints for getting general information from the server.")})
@ApplicationScoped
public class GeneralInfo extends EndpointProvider {
	
	@Inject
	@Location("tags/inputs/units/unitOptions")
	Template optionsTemplate;
	
	@ConfigProperty(name = "service.ops.currency")
	Currency currency;
	
	@GET
	@Path("currency")
	@Operation(
		summary = "The currency the base station is set to operate with."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Currency getCurrency(@Context SecurityContext ctx) {
		log.info("Getting currency of server.");
		return this.currency;
	}
	
	@GET
	@Path("units")
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
	public Map<String, List<Unit<?>>> getUnits(@Context SecurityContext ctx) {
		log.info("Getting valid unit list.");
		return UnitUtils.ALLOWED_UNITS_MAP;
	}
	
	@GET
	@Path("unitCompatibility")
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
	public Map<Unit<?>, Set<Unit<?>>> getUnitCompatibleMap(@Context SecurityContext ctx) {
		log.info("Getting unit set with lists of compatible units.");
		return UnitUtils.UNIT_COMPATIBILITY_MAP;
	}
	
	@GET
	@Path("unitCompatibility/{unit}")
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
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Response getUnitCompatible(
		@Context SecurityContext ctx,
		@HeaderParam("accept") String acceptHeaderVal,
		@PathParam("unit") String unitString
	) throws JsonProcessingException {
		log.info("Getting unit set with lists of compatible units. Accept header: {}", acceptHeaderVal);
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
		
		switch (acceptHeaderVal == null ? "" : acceptHeaderVal.strip()) {
			case MediaType.APPLICATION_JSON:
			case "":
				return Response.ok(units).build();
			case MediaType.TEXT_HTML:
				return Response.ok(
					optionsTemplate.data("units", units)
				).build();
			default:
				return Response.notAcceptable(Variant.encodings(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML).build()).build();
		}
	}
	
	
}
