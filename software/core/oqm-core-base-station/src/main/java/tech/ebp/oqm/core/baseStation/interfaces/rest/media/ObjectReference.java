package tech.ebp.oqm.core.baseStation.interfaces.rest.media;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;
import tech.ebp.oqm.core.baseStation.model.objReference.ObjRefImgOptions;
import tech.ebp.oqm.core.baseStation.model.objReference.ObjRefOptions;
import tech.ebp.oqm.core.baseStation.service.ObjReferenceService;
import tech.ebp.oqm.core.baseStation.utils.Roles;

/**
 */
@Slf4j
@Path("/api/media/objRef")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@RolesAllowed(Roles.INVENTORY_VIEW)
@RequestScoped
public class ObjectReference extends ApiProvider {

	@Inject
	ObjReferenceService objReferenceService;

	@GET
	@Operation(
		summary = "Gets an object reference string given the options."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getObjRefString(
		@Valid @BeanParam ObjRefOptions ops
	) {
		log.info("Getting object reference: {}.", ops);

		try {
			return Response.status(Response.Status.OK)
					   .entity(this.objReferenceService.getObjRef(ops))
					   .type(MediaType.TEXT_PLAIN_TYPE)
					   .build();
		} catch(IllegalArgumentException e){
			return Response.status(Response.Status.BAD_REQUEST)
					   .build();
		}
	}

	@GET
	@Path("parse/{ref}")
	@Operation(
		summary = "Gets an object reference string given the options."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Response parseObjRefString(
		@PathParam("ref") String objRef
	) {
		log.info("Parsing object reference: {}.", objRef);
		try {
			return Response.status(Response.Status.OK)
					   .entity(this.objReferenceService.parseObjReference(objRef))
					   .type(MediaType.APPLICATION_JSON)
					   .build();
		} catch(IllegalArgumentException e){
			return Response.status(Response.Status.BAD_REQUEST)
					   .build();
		}
	}

	@GET
	@Path("image")
	@Operation(
		summary = "Gets an object reference image string given the options."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@Produces("image/svg+xml")
	public Response getImage(
		@Valid @BeanParam ObjRefImgOptions ops
	) {
		log.info("Getting object reference image: {}.", ops);
		return Response.status(Response.Status.OK)
				   .entity(this.objReferenceService.getObjRefImage(ops))
				   .type("image/svg+xml")
				   .header("Content-Disposition", "attachment;filename=ref.svg")
				   .build();
	}
}
