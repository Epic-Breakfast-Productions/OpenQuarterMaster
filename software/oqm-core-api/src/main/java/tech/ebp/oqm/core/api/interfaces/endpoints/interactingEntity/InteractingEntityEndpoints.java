package tech.ebp.oqm.core.api.interfaces.endpoints.interactingEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.rest.search.InteractingEntitySearch;
import tech.ebp.oqm.core.api.service.mongo.InteractingEntityService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/interacting-entity")
@Tags({@Tag(name = "Interacting Entities", description = "Endpoints for dealing with interacting entities.")})
@RequestScoped
public class InteractingEntityEndpoints extends EndpointProvider {

	//TODO:: add search, get, history endpoints
	
	@Inject
	InteractingEntityService interactingEntityService;

	@GET
	@Operation(
		summary = "Searches the interacting entities."
	)
	@APIResponse(
		responseCode = "200",
		description = "Entities searched.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				type = SchemaType.OBJECT,
				implementation = SearchResult.class
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
	public Response search(
		@BeanParam InteractingEntitySearch search
	) {
		SearchResult<InteractingEntity> searchResult = this.getInteractingEntityService().search(search);
		return Response.status(Response.Status.OK)
			.entity(searchResult)
			.build();
	}

	@GET
	@Path("/self")
	@Operation(
		summary = "Gets an interacting entity."
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
	public Response getSelf() {
		return Response.ok(this.getInteractingEntity()).build();
	}
	
	@GET
	@Path("{entityId}")
	@Operation(
		summary = "Gets an interacting entity."
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
	public Response getInteractingEntity(
		@PathParam("entityId") String entityId
	) {
		return Response.ok(
			this.interactingEntityService.get(entityId)
		).build();
	}

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
	public Response getInteractingEntityReference(
		@PathParam("entityId") String entityId
	) {
		InteractingEntity entity = this.interactingEntityService.get(entityId);

		if(entity == null){
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		log.info("Getting reference for entity {} (from id {})", entity, entityId);
		return Response.ok(
			new InteractingEntityReference(entity)
		).build();
	}
}
