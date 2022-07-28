package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.inventory.storage;

import com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.MainObjectProvider;
import com.ebp.openQuarterMaster.baseStation.rest.search.StorageBlockSearch;
import com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingCalculations;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.lib.core.rest.ErrorMessage;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.tree.StorageBlockTree;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
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
import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Traced
@Slf4j
@Path("/api/storage/block")
@Tags({@Tag(name = "Storage Blocks", description = "Endpoints for managing Storage Blocks.")})
@RequestScoped
@NoArgsConstructor
public class StorageCrud extends MainObjectProvider<StorageBlock, StorageBlockSearch> {
	
	@Inject
	@Location("tags/search/storage/storageSearchResults.html")
	Template storageSearchResultsTemplate;
	
	@Inject
	public StorageCrud(
		StorageBlockService storageBlockService,
		UserService userService,
		JsonWebToken jwt
	) {
		super(storageBlockService, userService, jwt);
	}
	
	
	public String create(
		@Context SecurityContext securityContext,
		@Valid StorageBlock storageBlock
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Creating new storage block.");
		
		if (
			!this.getObjectService().list(
				and(
					eq("label", storageBlock.getLabel()),
					eq("location", storageBlock.getLocation()),
					eq("parent", storageBlock.getParent())
				),
				null,
				null
			).isEmpty()
		) {
			throw new BadRequestException(
				Response.status(
							Response.Status.BAD_REQUEST).entity(
							new ErrorMessage()
						)
						.build()
			);
		}
		
		if (storageBlock.getParent() != null) {
			StorageBlock parent = this.getObjectService().get(storageBlock.getParent());
			if (parent == null) {
				throw new IllegalArgumentException("No parent exists for parent given.");
			}
		}
		
		ObjectId output = this.getObjectService().add(storageBlock, this.getUserFromJwt());
		log.info("Storage block created with id: {}", output);
		return output.toHexString();
	}
	
	public Response search(
		@Context SecurityContext securityContext,
		@BeanParam StorageBlockSearch blockSearch
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Searching for storage blocks with: ");
		
		SearchResult<StorageBlock> output = this.getObjectService().search(blockSearch);
		
		if (output.getResults().isEmpty()) {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
		
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
							   .data("actionType", (blockSearch.getActionTypeHeaderVal() == null || blockSearch.getActionTypeHeaderVal().isBlank() ? "full" :
														blockSearch.getActionTypeHeaderVal()))
							   .data(
								   "searchFormId",
								   (blockSearch.getSearchFormIdHeaderVal() == null || blockSearch.getSearchFormIdHeaderVal().isBlank() ? "" : blockSearch.getSearchFormIdHeaderVal())
							   )
							   .data(
								   "inputIdPrepend",
								   (blockSearch.getInputIdPrependHeaderVal() == null || blockSearch.getInputIdPrependHeaderVal().isBlank() ? "" : blockSearch.getInputIdPrependHeaderVal())
							   )
							   .data(
								   "otherModalId",
								   (blockSearch.getOtherModalIdHeaderVal() == null || blockSearch.getOtherModalIdHeaderVal().isBlank() ? "" : blockSearch.getOtherModalIdHeaderVal())
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
		
		return ((StorageBlockService)this.getObjectService()).getStorageBlockTree(onlyInclude);
	}
}
