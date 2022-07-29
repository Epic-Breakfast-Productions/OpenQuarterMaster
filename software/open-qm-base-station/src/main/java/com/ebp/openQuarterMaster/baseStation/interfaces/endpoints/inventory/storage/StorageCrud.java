package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.inventory.storage;

import com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.MainObjectProvider;
import com.ebp.openQuarterMaster.baseStation.rest.search.StorageBlockSearch;
import com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingCalculations;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.tree.StorageBlockTree;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Traced
@Slf4j
@Path("/api/inventory/storage-block")
@Tags({@Tag(name = "Storage Blocks", description = "Endpoints for managing Storage Blocks.")})
@RequestScoped
@NoArgsConstructor
public class StorageCrud extends MainObjectProvider<StorageBlock, StorageBlockSearch> {
	
	Template storageSearchResultsTemplate;
	
	@Inject
	public StorageCrud(
		StorageBlockService storageBlockService,
		UserService userService,
		JsonWebToken jwt,
		@Location("tags/search/storage/storageSearchResults.html")
		Template storageSearchResultsTemplate
	) {
		super(StorageBlock.class, storageBlockService, userService, jwt);
		this.storageSearchResultsTemplate = storageSearchResultsTemplate;
	}
	
	@POST
	@Operation(
		summary = "Adds a new Storage Block."
	)
	@APIResponse(
		responseCode = "201",
		description = "Storage Block added.",
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
	@RolesAllowed("user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectId create(
		@Context SecurityContext securityContext,
		@Valid StorageBlock storageBlock
	) {
		return super.create(securityContext, storageBlock);
	}
	
	@GET
	@Operation(
		summary = "Gets a list of storage blocks."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					type = SchemaType.ARRAY
				)
			),
			@Content(
				mediaType = "text/html",
				schema = @Schema(type = SchemaType.STRING)
			)
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed("user")
	@Override
	public Response search(
		@Context SecurityContext securityContext,
		@BeanParam StorageBlockSearch blockSearch
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Searching for objects with: {}", blockSearch);
		
		SearchResult<StorageBlock> output = this.getObjectService().search(blockSearch);
		
		Response.ResponseBuilder rb = Response
										  .status(Response.Status.OK)
										  .header("num-elements", output.getResults().size())
										  .header("query-num-results", output.getNumResultsForEntireQuery());
		log.debug("Accept header value: \"{}\"", blockSearch.getAcceptHeaderVal());
		switch (blockSearch.getAcceptHeaderVal()) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				rb = rb.entity(
						   this.storageSearchResultsTemplate
							   .data("searchResults", output)
							   .data("actionType", (
								   blockSearch.getActionTypeHeaderVal() == null || blockSearch.getActionTypeHeaderVal().isBlank() ? "full" :
									   blockSearch.getActionTypeHeaderVal()
							   ))
							   .data(
								   "searchFormId",
								   (
									   blockSearch.getSearchFormIdHeaderVal() == null || blockSearch.getSearchFormIdHeaderVal().isBlank() ?
										   "" :
										   blockSearch.getSearchFormIdHeaderVal()
								   )
							   )
							   .data(
								   "inputIdPrepend",
								   (
									   blockSearch.getInputIdPrependHeaderVal() == null || blockSearch.getInputIdPrependHeaderVal().isBlank() ?
										   "" :
										   blockSearch.getInputIdPrependHeaderVal()
								   )
							   )
							   .data(
								   "otherModalId",
								   (
									   blockSearch.getOtherModalIdHeaderVal() == null || blockSearch.getOtherModalIdHeaderVal().isBlank() ?
										   "" :
										   blockSearch.getOtherModalIdHeaderVal()
								   )
							   )
							   .data("pagingCalculations", new PagingCalculations(blockSearch.getPagingOptions(false), output))
							   .data("storageService", this.getObjectService())
					   )
					   .type(MediaType.TEXT_HTML_TYPE);
				break;
			case MediaType.APPLICATION_JSON:
			default:
				log.debug("Requestor wanted json, or any other form");
				rb = rb.entity(output.getResults())
					   .type(MediaType.APPLICATION_JSON_TYPE);
		}
		
		return rb.build();
	}
	
	@GET
	@Path("tree")
	@Operation(
		summary = "Gets a tree of the storage blocks."
	)
	@APIResponse(
		responseCode = "200",
		description = "Tree retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					implementation = StorageBlockTree.class
				)
			)
		}
	)
	@APIResponse(
		responseCode = "204",
		description = "No items found from query given.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed("user")
	public StorageBlockTree tree(
		@Context SecurityContext securityContext,
		//for actual queries
		@QueryParam("onlyInclude") List<ObjectId> onlyInclude
	) {
		logRequestContext(this.getJwt(), securityContext);
		return ((StorageBlockService) this.getObjectService()).getStorageBlockTree(onlyInclude);
	}
}
