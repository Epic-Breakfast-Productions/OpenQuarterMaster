package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.inventory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.PassthroughProvider;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.AppliedTransactionSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.HistorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/inventory/item")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class InvItemPassthrough extends PassthroughProvider {
	
	@Getter
	@Inject
	@Location("tags/search/item/itemSearchResults")
	Template searchResultTemplate;
	
	@POST
	@Operation(
		summary = "Adds a new inventory item."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = "application/json"
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> create(
		ObjectNode item
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().invItemCreate(this.getBearerHeaderStr(), this.getSelectedDb(), item)
		);
	}
	
	//	@POST
	//	@Operation(
	//		summary = "Imports items from a file uploaded by a user."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object added.",
	//		content = @Content(
	//			mediaType = MediaType.APPLICATION_JSON,
	//			schema = @Schema(
	//				type = SchemaType.ARRAY
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@Consumes(MediaType.MULTIPART_FORM_DATA)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	public Uni<ArrayNode> importData(
	//		@BeanParam ImportBundleFileBody body
	//	) throws IOException {
	//		return this.getOqmCoreApiClient().invItemImportData(this.getBearerHeaderStr(), this.getSelectedDb(), body);
	//	}
	
	@Path("stats")
	@GET
	@Operation(
		summary = "Gets stats on this object's collection."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json"
		)
	)
	@Produces(MediaType.APPLICATION_JSON)
	@WithSpan
	public Uni<Response> getCollectionStats(
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().invItemCollectionStats(this.getBearerHeaderStr(), this.getSelectedDb())
		);
	}
	
	@GET
	@Operation(
		summary = "Gets a list of objects, using search parameters."
	)
	@APIResponse(
		responseCode = "200",
		description = "Items retrieved.",
		content = {
			@Content(
				mediaType = "application/json"
			)
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> search(
		//for actual queries
		@BeanParam InventoryItemSearch itemSearch,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId,
		@HeaderParam("otherModalId") String otherModalId,
		@HeaderParam("inputIdPrepend") String inputIdPrepend
	) {
		return this.handleCall(
			this.processSearchResults(
				this.getOqmCoreApiClient().invItemSearch(this.getBearerHeaderStr(), this.getSelectedDb(), itemSearch),
				this.searchResultTemplate,
				acceptType,
				searchFormId,
				otherModalId,
				inputIdPrepend,
				"select"
			)
		);
	}
	
	@Path("{id}")
	@GET
	@Operation(
		summary = "Gets a particular InventoryItem."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json"
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
	public Uni<Response> get(
		@PathParam("id") String id
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().invItemGet(this.getBearerHeaderStr(), this.getSelectedDb(), id)
		);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular Object.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object updated.",
		content = @Content(
			mediaType = "application/json"
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
	public Uni<Response> update(
		@PathParam("id") String id,
		ObjectNode updates
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().invItemUpdate(this.getBearerHeaderStr(), this.getSelectedDb(), id, updates)
		);
	}
	
	@DELETE
	@Path("{id}")
	@Operation(
		summary = "Deletes a particular object."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object deleted.",
		content = @Content(
			mediaType = "application/json"
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
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> delete(
		@PathParam("id") String id
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().invItemDelete(this.getBearerHeaderStr(), this.getSelectedDb(), id)
		);
	}
	
	@GET
	@Path("{id}/history")
	@Operation(
		summary = "Gets a particular object's history."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = {
			@Content(
				mediaType = "application/json"
			)
		}
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "No history found for object with that id.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> getHistoryForObject(
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId
	) {
		Uni<ObjectNode> searchUni = this.getOqmCoreApiClient().invItemGetHistoryForObject(this.getBearerHeaderStr(), this.getSelectedDb(), id, searchObject);
		return this.handleCall(
			this.processHistoryResults(searchUni, acceptType, searchFormId)
		);
	}
	
	@GET
	@Path("history")
	@Operation(
		summary = "Searches the history for the inventory items."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json"
			)
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().invItemSearchHistory(this.getBearerHeaderStr(), this.getSelectedDb(), searchObject)
		);
	}
	
	//	@GET
	//	@Path("{itemId}/stored/{storageBlockId}")
	//	@Operation(
	//		summary = "Gets the stored amount or tracked item to the storage block specified."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Item added.",
	//		content = @Content(
	//			mediaType = "application/json"
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "No item found to get.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	public Uni<ObjectNode> getStoredInventoryItem(
	//		@PathParam("itemId") String itemId,
	//		@PathParam("storageBlockId") String storageBlockId
	//	) {
	//		//TODO
	//		return Response.serverError().entity("Not implemented yet.").build();
	//	}
	
}
