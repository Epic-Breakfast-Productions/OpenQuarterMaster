package tech.ebp.oqm.baseStation.interfaces.endpoints.media.files;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainFileObjectProvider;
import tech.ebp.oqm.baseStation.rest.file.FileAttachmentUploadBody;
import tech.ebp.oqm.baseStation.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.baseStation.service.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.lib.core.object.media.file.FileAttachment;
import tech.ebp.oqm.lib.core.rest.auth.roles.Roles;
import tech.ebp.oqm.lib.core.rest.media.file.FileAttachmentGet;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.stream.Collectors;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Traced
@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/media/fileAttachments")
@Tags({@Tag(name = "File Attachments", description = "Endpoints for File Attachments.")})
@RequestScoped
public class FileAttachmentCrud extends MainFileObjectProvider<FileAttachment, FileAttachmentSearch, FileAttachmentUploadBody> {
	
	Template fileAttachmentSearchResultsTemplate;
	
	@Inject
	public FileAttachmentCrud(
		FileAttachmentService objectService,
		InteractingEntityService interactingEntityService,
		JsonWebToken jwt,
		@Location("tags/objView/history/searchResults.html")
		Template historyRowsTemplate,
		@Location("tags/search/item/itemSearchResults.html")
			Template fileAttachmentSearchResultsTemplate
	) {
		super(
			objectService,
			interactingEntityService,
			jwt,
			historyRowsTemplate
		);
		this.fileAttachmentSearchResultsTemplate = fileAttachmentSearchResultsTemplate;
	}
	
	@POST
	@Operation(
		summary = "Adds a new file attachment."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
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
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	//	@Override
	public Response add(
		@Context SecurityContext securityContext,
		@BeanParam FileAttachmentUploadBody body
	) throws IOException {
		FileAttachment newAttachmentObj = new FileAttachment();
		
		this.getFileService().add(
			newAttachmentObj,
			body,
			this.getInteractingEntityFromJwt()
		);
		
		return Response.ok(newAttachmentObj.getId()).build();
	}
	
	@GET
	@Operation(
		summary = "Searches for file attachments."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
			schema = @Schema(
				type = SchemaType.ARRAY,
				implementation = FileAttachmentGet.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	//	@Override
	public Response search(
		@Context SecurityContext securityContext,
		@BeanParam FileAttachmentSearch search
	) throws IOException {
		Tuple2<Response.ResponseBuilder, SearchResult<FileAttachment>> results = this.getSearchResponseBuilder(securityContext, search);
		SearchResult<FileAttachment> originalResult = results.getItem2();
		
		SearchResult<FileAttachmentGet> output = new SearchResult<>(
			results.getItem2().getResults()
				.stream()
				.map((FileAttachment a)->{
					return FileAttachmentGet.fromFileAttachment(a, this.getFileService().getRevisions(a.getId()));
				})
				.collect(Collectors.toList()),
			originalResult.getNumResultsForEntireQuery(),
			originalResult.isHadSearchQuery(),
			originalResult.getPagingOptions()
		);
		
		
		Response.ResponseBuilder rb = this.getSearchResultResponseBuilder(output);;
		
		log.debug("Accept header value: \"{}\"", search.getAcceptHeaderVal());
		switch (search.getAcceptHeaderVal()) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				rb = rb.entity(
						this.fileAttachmentSearchResultsTemplate
							.data("searchResults", output)
							.data("actionType", (
								search.getActionTypeHeaderVal() == null || search.getActionTypeHeaderVal().isBlank() ? "full" :
									search.getActionTypeHeaderVal()
							))
							.data(
								"searchFormId",
								(
									search.getSearchFormIdHeaderVal() == null || search.getSearchFormIdHeaderVal().isBlank() ?
										"" :
										search.getSearchFormIdHeaderVal()
								)
							)
							.data(
								"inputIdPrepend",
								(
									search.getInputIdPrependHeaderVal() == null || search.getInputIdPrependHeaderVal().isBlank() ?
										"" :
										search.getInputIdPrependHeaderVal()
								)
							)
							.data(
								"otherModalId",
								(
									search.getOtherModalIdHeaderVal() == null || search.getOtherModalIdHeaderVal().isBlank() ?
										"" :
										search.getOtherModalIdHeaderVal()
								)
							)
							.data("pagingCalculations", new PagingCalculations(output))
							.data("storageService", this.getFileService().getFileObjectService())
					)
						 .type(MediaType.TEXT_HTML_TYPE);
				break;
			case MediaType.APPLICATION_JSON:
			default:
				log.debug("Requestor wanted json, or any other form");
		}
		
		return rb.build();
	}
	
	@GET
	@Path("{id}")
	@Operation(
		summary = "Gets a particular file attachment details."
	)
	@APIResponse(
		responseCode = "200",
		description = "File information retrieved.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
			schema = @Schema(
				implementation = FileAttachmentGet.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	//	@Override
	public Response get(
		@Context SecurityContext securityContext,
		@PathParam("id")
		String id
	) throws IOException {
		return Response.ok(
			FileAttachmentGet.fromFileAttachment(
				this.getFileService().getFileObjectService().get(id),
				this.getFileService().getRevisions(new ObjectId(id))
			)
		).build();
	}
}
