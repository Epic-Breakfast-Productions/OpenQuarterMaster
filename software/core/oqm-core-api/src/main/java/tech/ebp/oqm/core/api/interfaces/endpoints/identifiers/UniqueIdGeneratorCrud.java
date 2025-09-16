package tech.ebp.oqm.core.api.interfaces.endpoints.identifiers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueIdGenResult;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueIdentifierGenerator;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.StorageBlockSearch;
import tech.ebp.oqm.core.api.model.rest.search.UniqueIdGeneratorSearch;
import tech.ebp.oqm.core.api.model.rest.tree.storageBlock.StorageBlockTree;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.UniqueIdentifierGenerationService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;

import java.util.List;
import java.util.Optional;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1_DB_AWARE + "/identifiers/unique/generator")
@Tags({@Tag(name = "Unique IDs / Generation", description = "Endpoints for managing Unique ID Generators, and associated unique id endpoints.")})
@RequestScoped
public class UniqueIdGeneratorCrud extends MainObjectProvider<UniqueIdentifierGenerator, UniqueIdGeneratorSearch> {
	
	@Inject
	@Getter
	UniqueIdentifierGenerationService objectService;
	
	@Getter
	Class<UniqueIdentifierGenerator> objectClass = UniqueIdentifierGenerator.class;
	
	@POST
	@Operation(
		summary = "Adds a new Unique Identifier Generator."
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
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ObjectId create(
		@Valid UniqueIdentifierGenerator generator
	) {
		return super.create(generator);
	}
	
	@GET
	@Operation(
		summary = "Gets a list of unique id generators, using search parameters."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved."
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Override
	public SearchResult<UniqueIdentifierGenerator> search(
		@BeanParam UniqueIdGeneratorSearch search
	) {
		return super.search(search);
	}
	
	@Override
	@Path("stats")
	@GET
	@Operation(
		summary = "Gets stats on this object's collection."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = CollectionStats.class
			)
		)
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public CollectionStats getCollectionStats(
	) {
		return super.getCollectionStats();
	}
	
	@Path("{id}")
	@GET
	@Operation(
		summary = "Gets a particular Unique Id Generator."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = UniqueIdentifierGenerator.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Override
	public UniqueIdentifierGenerator get(
		@PathParam("id") String id
	) {
		return super.get(id);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a Unique Id Generator.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "UniqueIdentifierGenerator updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = UniqueIdentifierGenerator.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public UniqueIdentifierGenerator update(
		@PathParam("id") String id,
		ObjectNode updates
	) {
		return super.update(id, updates);
	}
	
	@DELETE
	@Path("{id}")
	@Operation(
		summary = "Deletes a particular UniqueIdentifierGenerator."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object deleted.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = UniqueIdentifierGenerator.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has already been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "No object found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public UniqueIdentifierGenerator delete(
		@PathParam("id") String id
	) {
		return super.delete(id);
	}
	
	@GET
	@Path("{id}/generate")
	@Operation(
		summary = "Generates a new id from the given generator."
	
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = UniqueIdentifierGenerator.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public UniqueIdGenResult generateNewId(
		@PathParam("id") String id,
		@QueryParam("num") Optional<Integer> numToGenerate
	) {
		return this.getObjectService().getNextNUniqueIds(
			this.getOqmDbIdOrName(),
			new ObjectId(id),
			numToGenerate.orElse(1)
		);
	}
	
}
