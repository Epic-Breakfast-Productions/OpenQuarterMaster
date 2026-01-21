package tech.ebp.oqm.core.api.interfaces.endpoints;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.core.api.model.object.FileMainObject;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;
import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.model.rest.search.FileSearchObject;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.service.mongo.file.MongoHistoriedFileService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.mongo.utils.FileContentsGet;

import java.io.IOException;

/**
 * Main abstract method to handle standard CRUD operations for MainObjects
 * <p>
 *     TODO:: comb through to ensure proper logs
 *
 * @param <T>
 * @param <S>
 */
@Slf4j
@NoArgsConstructor
public abstract class MainFileObjectProvider<T extends FileMainObject, U extends FileUploadBody, S extends FileSearchObject<T>, G extends FileGet> extends ObjectProvider {
	
	
	@Getter
	@PathParam("oqmDbIdOrName")
	String oqmDbIdOrName;
	
	public abstract MongoHistoriedFileService<T, U, S, G> getFileService();
	
	protected Response.ResponseBuilder getSearchResponseBuilder(
		String oqmDbIdOrName,
		S searchObject
	) {
		return this.getSearchResultResponseBuilder(this.getFileService().search(oqmDbIdOrName, searchObject));
	}
	
	//<editor-fold desc="CRUD operations">
	
	
	//	@GET
	//	@Operation(
	//		summary = "Searches for files"
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object retrieved.",
	//		content = @Content(
	//			mediaType = "application/json",
	//			schema = @Schema(
	//				implementation = MainObject.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "Bad request given, could not find object at given id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "410",
	//		description = "Object requested has been deleted.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public Response search(
		//		@BeanParam
		S searchObject
	) {
		//TODO:: this should produce results of the G type, not T
		return this.getSearchResponseBuilder(this.getOqmDbIdOrName(), searchObject).build();
	}
	
	protected abstract T getFileObjFromUpload(U upload);
	
	//	@POST
	//	@Operation(
	//		summary = "Adds a file."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object added.",
	//		content = @Content(
	//			mediaType = MediaType.APPLICATION_JSON,
	//			schema = @Schema(
	//				implementation = ObjectId.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@RolesAllowed(Roles.INVENTORY_ADMIN)
	//	@Consumes(MediaType.MULTIPART_FORM_DATA)
	//	@Produces(MediaType.APPLICATION_JSON)
	public Response add(
		//@BeanParam
		U body
	) throws IOException {
		return Response.ok(this.getFileService().add(
			this.getOqmDbIdOrName(),
			this.getFileObjFromUpload(body),
			body,
			this.getInteractingEntity()
		)).build();
	}
	
	//	@Path("{id}")
	//	@GET
	//	@Operation(
	//		summary = "Gets a particular object."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object retrieved.",
	//		content = @Content(
	//			mediaType = "application/json",
	//			schema = @Schema(
	//				implementation = MainObject.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "Bad request given, could not find object at given id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "410",
	//		description = "Object requested has been deleted.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public G get(
		//		@PathParam("id")
		String id
	) {
		log.info("Retrieving object with id {}", id);
		return this.getFileService().getObjGet(this.getOqmDbIdOrName(), id);
	}
	
	//	@PUT
	//	@Path("{id}")
	//	@Operation(
	//		summary = "Updates a particular Object.",
	//		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object updated.",
	//		content = @Content(
	//			mediaType = "application/json",
	//			schema = @Schema(
	//				implementation = MainObject.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "Bad request given, could not find object at given id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "410",
	//		description = "Object requested has been deleted.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@RolesAllowed(UserRoles.INVENTORY_EDIT)
	//	@Produces(MediaType.APPLICATION_JSON)
	public Integer updateFile(
		//		@PathParam("id")
		String id,
		//		@BeanParam
		U body
	) throws IOException {
		return this.getFileService().updateFile(this.getOqmDbIdOrName(), null, id, body, this.getInteractingEntity());
	}
	
	public G updateObj(
		//		@PathParam("id")
		String id,
		//		@BeanParam
		ObjectNode updates
	) {
		return this.getFileService().fileObjToGet(
			this.getOqmDbIdOrName(),
			this.getFileService().getFileObjectService().update(
				this.getOqmDbIdOrName(),
				null,
				id,
				updates,
				this.getInteractingEntity()
			)
		);
	}
	
	protected <A> A getRevision(String id, String revision, Class<A> aClass) throws IOException {
		int revisionNum;
		if ("latest".equalsIgnoreCase(revision)) {
			revisionNum = this.getFileService().getLatestVersionNum(this.getOqmDbIdOrName(), id);
		} else if ("first".equalsIgnoreCase(revision)) {
			revisionNum = 1;
		} else {
			revisionNum = Integer.parseInt(revision);
		}
		
		if (aClass == FileMetadata.class) {
			return (A) this.getFileService().getFileMetadata(this.getOqmDbIdOrName(), id, revisionNum);
		} else if (aClass == FileContentsGet.class) {
			return (A) this.getFileService().getFile(this.getOqmDbIdOrName(), id, revisionNum);
		}
		throw new IllegalArgumentException("Illegal aClss given: " + aClass.getSimpleName());
	}
	
	//	@Path("{id}/revision/{rev}")
	//	@GET
	//	@Operation(
	//		summary = "Gets a particular object."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object retrieved.",
	//		content = @Content(
	//			mediaType = "application/json",
	//			schema = @Schema(
	//				implementation = MainObject.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "Bad request given, could not find object at given id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "410",
	//		description = "Object requested has been deleted.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//		@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public FileMetadata getRevision(
		//		@PathParam("id")
		String id,
		//		@PathParam("rev")
		String revision
	) throws IOException {
		//TODO
		log.info("Retrieving object with id {}", id);
		return this.getRevision(id, revision, FileMetadata.class);
	}
	
	//	@Path("{id}/revision/{rev}/data")
	//	@GET
	//	@Operation(
	//		summary = "Gets a particular object."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object retrieved.",
	//		content = @Content(
	//			mediaType = "application/json",
	//			schema = @Schema(
	//				implementation = MainObject.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "Bad request given, could not find object at given id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "410",
	//		description = "Object requested has been deleted.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public Response getRevisionData(
		//		@PathParam("id")
		String id,
		//		@PathParam("rev")
		String revision
	) throws IOException {
		FileContentsGet fileContentsGet = this.getRevision(id, revision, FileContentsGet.class);
		Response.ResponseBuilder response = Response.ok(
			fileContentsGet.getContents()
		);
		response.header(HttpHeaders.CONTENT_TYPE, fileContentsGet.getMetadata().getMimeType());
		//TODO:: update filename with new extension from conversion
		response.header("Content-Disposition", "attachment;filename=" + fileContentsGet.getMetadata().getOrigName());
		response.header("hash-md5", fileContentsGet.getMetadata().getHashes().getMd5());
		response.header("hash-sha1", fileContentsGet.getMetadata().getHashes().getSha1());
		response.header("hash-sha256", fileContentsGet.getMetadata().getHashes().getSha256());
		response.header("upload-datetime", fileContentsGet.getMetadata().getUploadDateTime());
		return response.build();
	}
	
	
	//	@Path("{id}")
	//	@DELETE
	//	@Operation(
	//		summary = "Removes a particular file."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object retrieved.",
	//		content = @Content(
	//			mediaType = "application/json",
	//			schema = @Schema(
	//				implementation = MainObject.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "Bad request given, could not find object at given id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "410",
	//		description = "Object requested has been deleted.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(Roles.INVENTORY_EDIT)
	public G remove(
		//		@PathParam("id")
		String id
	) {
		log.info("Retrieving object with id {}", id);
		return this.getFileService().removeFile(this.getOqmDbIdOrName(), null, new ObjectId(id), this.getInteractingEntity());
	}
	
	//</editor-fold>
	
	//<editor-fold desc="History">
	
	//	@GET
	//	@Path("{id}/history")
	//	@Operation(
	//		summary = "Gets a particular object's history."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object retrieved.",
	//		content = {
	//			@Content(
	//				mediaType = "application/json",
	//				schema = @Schema(type = SchemaType.ARRAY, implementation = ObjectHistoryEvent.class)
	//			)
	//		}
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "No history found for object with that id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response getHistoryForObject(
//		@PathParam("id")
		String id,
		//		@BeanParam
		HistorySearch searchObject
	) {
		log.info("Retrieving specific {} history with id {} from REST interface", this.getFileService().getClazz().getSimpleName(), id);
		
		searchObject.setObjectId(new ObjectId(id));
		
		SearchResult<ObjectHistoryEvent> searchResult = this.getFileService().getFileObjectService().searchHistory(this.getOqmDbIdOrName(), searchObject, false);
		return this.getSearchResultResponseBuilder(searchResult).build();
	}
	
	//	@GET
	//	@Path("history")
	//	@Operation(
	//		summary = "Searches the history for the objects."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Blocks retrieved.",
	//		content = {
	//			@Content(
	//				mediaType = "application/json",
	//				schema = @Schema(
	//					type = SchemaType.ARRAY,
	//					implementation = ObjectHistoryEvent.class
	//				)
	//			)
	//		},
	//		headers = {
	//			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
	//			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
	//		}
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public SearchResult<ObjectHistoryEvent> searchHistory(
//		@BeanParam
		HistorySearch searchObject
	) {
		log.info("Searching for objects with: {}", searchObject);
		
		return this.getFileService().getFileObjectService().searchHistory(this.getOqmDbIdOrName(), searchObject, false);
	}
	//</editor-fold>
}
