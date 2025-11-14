package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.HistorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ImageSearch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/media/image")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ImagePassthrough extends PassthroughProvider {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@Getter
	@Inject
	@Location("tags/search/image/imageSearchResults")
	Template searchResultTemplate;
	
	@GET
	@Operation(
		summary = "Searches for images"
	)
	@APIResponse(
		responseCode = "200",
		description = "Searched.",
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
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@WithSpan
	public Uni<Response> search(
		@BeanParam ImageSearch searchObject,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId,
		@HeaderParam("otherModalId") String otherModalId,
		@HeaderParam("inputIdPrepend") String inputIdPrepend
	) {
		return this.handleCall(
			this.processSearchResults(
				this.oqmCoreApiClient.imageSearch(this.getBearerHeaderStr(), this.getSelectedDb(), searchObject),
				this.searchResultTemplate,
				acceptType,
				searchFormId,
				otherModalId,
				inputIdPrepend,
				"select"
			)
		);
	}
	
	@POST
	@Operation(
		summary = "Adds a file."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> add(
		@BeanParam FileUploadBody body
	) throws IOException {
		return this.handleCall(
			this.oqmCoreApiClient.imageAdd(this.getBearerHeaderStr(), this.getSelectedDb(), body)
		);
	}
	
	@Path("{id}")
	@GET
	@Operation(
		summary = "Gets a particular file object, not the file itself."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON
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
	@WithSpan
	public Uni<Response> get(
		@PathParam("id") String id
	) {
		return this.handleCall(
			this.oqmCoreApiClient.imageGet(this.getBearerHeaderStr(), this.getSelectedDb(), id)
		);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular image, adds a new revision.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = Integer.class
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
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@WithSpan
	public Uni<Response> updateFile(
		@PathParam("id") String id,
		@BeanParam FileUploadBody body
	) {
		return this.handleCall(
			this.oqmCoreApiClient.imageUpdateFile(this.getBearerHeaderStr(), this.getSelectedDb(), id, body)
		);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular file's Object.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = Integer.class
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@WithSpan
	public Uni<Response> updateObj(
		@PathParam("id")
		String id,
		ObjectNode updates
	) {
		return this.handleCall(
			this.oqmCoreApiClient.imageUpdateObj(this.getBearerHeaderStr(), this.getSelectedDb(), id, updates)
		);
	}
	
	@Path("{id}/revision/{rev}")
	@GET
	@Operation(
		summary = "Gets a particular file revision's metadata."
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
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public Uni<Response> getRevision(
		@PathParam("id")
		String id,
		@PathParam("rev")
		String revision
	) {
		return this.handleCall(
			this.oqmCoreApiClient.imageGetRevision(this.getBearerHeaderStr(), this.getSelectedDb(), id, revision)
		);
	}
	
	@Blocking
	@Path("{id}/revision/{rev}/data")
	@GET
	@Operation(
		summary = "Gets a particular file revision's data."
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
	@Produces("*/*")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public Uni<Response> getRevisionData(
		@PathParam("id")
		String id,
		@PathParam("rev")
		String revision
	) {
		return this.handleCall(
			this.oqmCoreApiClient.imageGetRevisionData(this.getBearerHeaderStr(), this.getSelectedDb(), id, revision)
		);
	}
	
	@GET
	@Path("{id}/history")
	@Operation(
		summary = "Gets a particular file's history."
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
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public Uni<Response> getHistoryForObject(
		@PathParam("id") String id,
		@BeanParam HistorySearch historySearch,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId
	) {
		Uni<ObjectNode> searchUni = this.getOqmCoreApiClient().imageGetHistoryForObject(this.getBearerHeaderStr(), this.getSelectedDb(), id, historySearch);
		return this.handleCall(
			this.processHistoryResults(searchUni, acceptType, searchFormId)
		);
	}
	
	@GET
	@Path("history")
	@Operation(
		summary = "Searches the history for the objects."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json"
			)
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public Uni<Response> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		return this.handleCall(
			this.oqmCoreApiClient.imageSearchHistory(this.getBearerHeaderStr(), this.getSelectedDb(), searchObject)
		);
	}
	
	@Blocking
	@GET
	@Path("/for/{type}/{id}")
	public Uni<Response> imageForObj(@PathParam("type") String type, @PathParam("id") String objId) {
		return this.handleCall(
			this.oqmCoreApiClient.imageForObject(this.getBearerHeaderStr(), this.getSelectedDb(), type, objId)
		);
	}
	
}
