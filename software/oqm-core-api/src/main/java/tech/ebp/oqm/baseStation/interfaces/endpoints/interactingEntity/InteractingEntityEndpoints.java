package tech.ebp.oqm.baseStation.interfaces.endpoints.interactingEntity;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/interacting-entity")
@Tags({@Tag(name = "Interacting Entities", description = "Endpoints for dealing with interacting entities.")})
@RequestScoped
public class InteractingEntityEndpoints extends EndpointProvider {

	//TODO:: add search, get, history endpoints
	
	@Inject
	InteractingEntityService interactingEntityService;
	
	@GET
	@Path("{entityId}/reference")
	@Operation(
		summary = "Gets the reference object for an interacting entity."
	)
	@APIResponse(
		responseCode = "200",
		description = "Item added.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				type = SchemaType.ARRAY,
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "No item found to get.",
		content = @Content(mediaType = "text/plain")
	)
	@Authenticated
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInventoryItemsInBlock(
		@PathParam("entityId") String entityId
	) {
		return Response.ok(
			new InteractingEntityReference(this.interactingEntityService.get(entityId))
		).build();
	}
}
