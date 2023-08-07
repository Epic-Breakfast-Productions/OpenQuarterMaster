package tech.ebp.oqm.baseStation.interfaces.endpoints;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.rest.file.FileUploadBody;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.file.MongoHistoriedFileService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

import javax.ws.rs.BeanParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Main abstract method to handle standard CRUD operations for MainObjects
 * <p>
 *
 * @param <T>
 * @param <S>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MainFileObjectProvider<T extends FileMainObject, S extends SearchObject<T>, F extends FileUploadBody> extends ObjectProvider {
	
	@Getter
	private MongoHistoriedFileService<T, S> fileService;
	@Getter
	private Template historyRowsTemplate;
	
	protected MainFileObjectProvider(
		JsonWebToken jwt,
		InteractingEntityService interactingEntityService,
		SecurityContext securityContext,
		MongoHistoriedFileService<T, S> fileService,
		Template historyRowsTemplate
	) {
		super(jwt, interactingEntityService, securityContext);
		this.fileService = fileService;
		this.historyRowsTemplate = historyRowsTemplate;
	}
	
	@WithSpan
	protected Tuple2<Response.ResponseBuilder, SearchResult<T>> getSearchResponseBuilder(
		@BeanParam S searchObject
	) {
		SearchResult<T> searchResult = this.getFileService().getFileObjectService().search(searchObject, false);
		
		return Tuple2.of(
			this.getSearchResultResponseBuilder(searchResult),
			searchResult
		);
	}
	
	
	
	//<editor-fold desc="CRUD operations">
	
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
//	public abstract Response add(
//		@Context SecurityContext securityContext,
//		@BeanParam F body
//	) throws IOException;
	
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
//			),
//			@Content(
//				mediaType = "text/html",
//				schema = @Schema(type = SchemaType.STRING)
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
//	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
//	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public Response getHistoryForObject(
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject,
		@HeaderParam("accept") String acceptHeaderVal,
		@HeaderParam("searchFormId") String searchFormId
	) {
		log.info("Retrieving specific {} history with id {} from REST interface", this.getFileService().getClazz().getSimpleName(), id);
		
		searchObject.setObjectId(new ObjectId(id));
		
		SearchResult<ObjectHistoryEvent> searchResult = this.getFileService().getFileObjectService().searchHistory(searchObject, false);
		
		
		log.info("Found {} history events matching query.", searchResult.getNumResultsForEntireQuery());
		
		Response.ResponseBuilder rb = this.getSearchResultResponseBuilder(searchResult);
		log.debug("Accept header value: \"{}\"", acceptHeaderVal);
		switch (acceptHeaderVal) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				rb = rb.entity(
						   this.getHistoryRowsTemplate()
							   .data("searchFormId", searchFormId)
							   .data("searchResults", searchResult)
							   .data("interactingEntityService", this.getInteractingEntityService())
							   .data("pagingCalculations", new PagingCalculations(searchResult))
					   )
					   .type(MediaType.TEXT_HTML_TYPE);
				break;
			case MediaType.APPLICATION_JSON:
			default:
				log.debug("Requestor wanted json, or any other form");
		}
		return rb.build();
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
	//	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	@WithSpan
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		log.info("Searching for objects with: {}", searchObject);
		
		return this.getFileService().getFileObjectService().searchHistory(searchObject, false);
	}
	//</editor-fold>
}
