package tech.ebp.oqm.baseStation.interfaces.endpoints.media.files;

// TODO:: reenable once working #51

//@Slf4j
//@Path(ROOT_API_ENDPOINT_V1 + "/media/fileAttachments")
//@Tags({@Tag(name = "File Attachments", description = "Endpoints for File Attachments.")})
//@RequestScoped
//public class FileAttachmentCrud extends MainFileObjectProvider<FileAttachment, FileAttachmentSearch, FileAttachmentUploadBody> {
//
//	Template fileAttachmentSearchResultsTemplate;
//
//	@Inject
//	public FileAttachmentCrud(
//		FileAttachmentService objectService,
//		InteractingEntityService interactingEntityService,
//		JsonWebToken jwt,
//		@Location("tags/objView/history/searchResults.html")
//		Template historyRowsTemplate,
//		@Location("tags/search/item/itemSearchResults.html")
//			Template fileAttachmentSearchResultsTemplate
//	) {
//		super(
//			objectService,
//			interactingEntityService,
//			jwt,
//			historyRowsTemplate
//		);
//		this.fileAttachmentSearchResultsTemplate = fileAttachmentSearchResultsTemplate;
//	}
//
//	@POST
//	@Operation(
//		summary = "Adds a new file attachment."
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
//	@RolesAllowed(Roles.INVENTORY_EDIT)
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Produces(MediaType.APPLICATION_JSON)
//	//	@Override
//	public Response add(
//		@BeanParam FileAttachmentUploadBody body
//	) throws IOException {
//		FileAttachment newAttachmentObj = new FileAttachment();
//
//		this.getFileService().add(
//			newAttachmentObj,
//			body,
//			this.getInteractingEntityFromJwt()
//		);
//
//		return Response.ok(newAttachmentObj.getId()).build();
//	}
//
//	@GET
//	@Operation(
//		summary = "Searches for file attachments."
//	)
//	@APIResponse(
//		responseCode = "200",
//		description = "Object added.",
//		content = @Content(
//			mediaType = MediaType.APPLICATION_JSON,
//			schema = @Schema(
//				type = SchemaType.ARRAY,
//				implementation = FileAttachmentGet.class
//			)
//		)
//	)
//	@APIResponse(
//		responseCode = "400",
//		description = "Bad request given. Data given could not pass validation.",
//		content = @Content(mediaType = "text/plain")
//	)
//	@RolesAllowed(Roles.INVENTORY_EDIT)
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Produces(MediaType.APPLICATION_JSON)
//	//	@Override
//	public Response search(
//		@Context SecurityContext securityContext,
//		@BeanParam FileAttachmentSearch search
//	) throws IOException {
//		Tuple2<Response.ResponseBuilder, SearchResult<FileAttachment>> results = this.getSearchResponseBuilder(securityContext, search);
//		SearchResult<FileAttachment> originalResult = results.getItem2();
//
//		SearchResult<FileAttachmentGet> output = new SearchResult<>(
//			results.getItem2().getResults()
//				.stream()
//				.map((FileAttachment a)->{
//					return FileAttachmentGet.fromFileAttachment(a, this.getFileService().getRevisions(a.getId()));
//				})
//				.collect(Collectors.toList()),
//			originalResult.getNumResultsForEntireQuery(),
//			originalResult.isHadSearchQuery(),
//			originalResult.getPagingOptions()
//		);
//
//
//		Response.ResponseBuilder rb = this.getSearchResultResponseBuilder(output);;
//
//		log.debug("Accept header value: \"{}\"", search.getAcceptHeaderVal());
//		switch (search.getAcceptHeaderVal()) {
//			case MediaType.TEXT_HTML:
//				log.debug("Requestor wanted html.");
//				rb = rb.entity(
//						this.fileAttachmentSearchResultsTemplate
//							.data("searchResults", output)
//							.data("actionType", (
//								search.getActionTypeHeaderVal() == null || search.getActionTypeHeaderVal().isBlank() ? "full" :
//									search.getActionTypeHeaderVal()
//							))
//							.data(
//								"searchFormId",
//								(
//									search.getSearchFormIdHeaderVal() == null || search.getSearchFormIdHeaderVal().isBlank() ?
//										"" :
//										search.getSearchFormIdHeaderVal()
//								)
//							)
//							.data(
//								"inputIdPrepend",
//								(
//									search.getInputIdPrependHeaderVal() == null || search.getInputIdPrependHeaderVal().isBlank() ?
//										"" :
//										search.getInputIdPrependHeaderVal()
//								)
//							)
//							.data(
//								"otherModalId",
//								(
//									search.getOtherModalIdHeaderVal() == null || search.getOtherModalIdHeaderVal().isBlank() ?
//										"" :
//										search.getOtherModalIdHeaderVal()
//								)
//							)
//							.data("pagingCalculations", new PagingCalculations(output))
//							.data("storageService", this.getFileService().getFileObjectService())
//					)
//						 .type(MediaType.TEXT_HTML_TYPE);
//				break;
//			case MediaType.APPLICATION_JSON:
//			default:
//				log.debug("Requestor wanted json, or any other form");
//		}
//
//		return rb.build();
//	}
//
//	@GET
//	@Path("{id}")
//	@Operation(
//		summary = "Gets a particular file attachment details."
//	)
//	@APIResponse(
//		responseCode = "200",
//		description = "File information retrieved.",
//		content = @Content(
//			mediaType = MediaType.APPLICATION_JSON,
//			schema = @Schema(
//				implementation = FileAttachmentGet.class
//			)
//		)
//	)
//	@APIResponse(
//		responseCode = "400",
//		description = "Bad request given. Data given could not pass validation.",
//		content = @Content(mediaType = "text/plain")
//	)
//	@RolesAllowed(Roles.INVENTORY_EDIT)
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Produces(MediaType.APPLICATION_JSON)
//	//	@Override
//	public Response get(
//		@Context SecurityContext securityContext,
//		@PathParam("id")
//		String id
//	) throws IOException {
//		logRequestContext(this.getJwt(), securityContext);
//		return Response.ok(
//			FileAttachmentGet.fromFileAttachment(
//				this.getFileService().getFileObjectService().get(id),
//				this.getFileService().getRevisions(new ObjectId(id))
//			)
//		).build();
//	}
//
//	@GET
//	@Path("{id}/data")
//	@Operation(
//		summary = "Gets the data for the latest revision of a file."
//	)
//	@APIResponse(
//		responseCode = "200",
//		description = "File information retrieved.",
//		content = @Content(
//			mediaType = MediaType.APPLICATION_JSON,
//			schema = @Schema(
//				implementation = FileAttachmentGet.class
//			)
//		)
//	)
//	@APIResponse(
//		responseCode = "400",
//		description = "Bad request given. Data given could not pass validation.",
//		content = @Content(mediaType = "text/plain")
//	)
//	@RolesAllowed(Roles.INVENTORY_EDIT)
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Produces(MediaType.APPLICATION_JSON)
//	//	@Override
//	public Response getLatestData(
//		@Context SecurityContext securityContext,
//		@PathParam("id")
//		String id
//	) throws IOException {
//		logRequestContext(this.getJwt(), securityContext);
//
//		FileContentsGet fileContentsGet = this.getFileService().getLatestFile(id);
//		Response.ResponseBuilder response = Response.ok(
//			fileContentsGet.getContents()
//		);
//		response.header("Content-Disposition", "attachment;filename="+fileContentsGet.getMetadata().getOrigName());
//		response.header("hash-md5", fileContentsGet.getMetadata().getHashes().getMd5());
//		response.header("hash-sha1", fileContentsGet.getMetadata().getHashes().getSha1());
//		response.header("hash-sha256", fileContentsGet.getMetadata().getHashes().getSha256());
//		response.header("upload-datetime", fileContentsGet.getMetadata().getUploadDateTime());
//		return response.build();
//	}
//}
