package com.ebp.openQuarterMaster.baseStation.endpoints.info;

import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Traced
@Slf4j
@Path("/api/media/runByImage")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@RequestScoped
public class RunByUtils extends EndpointProvider {
	
	@Inject
	JsonWebToken jwt;
	
	@ConfigProperty(name = "service.runBy.image", defaultValue = "/")
	File runByImage;
	
	@GET
	@Operation(
		summary = "Gets a particular image's data string for use in html images."
	)
	//    @APIResponse(
	//            responseCode = "200",
	//            description = "Image retrieved."
	////            content = @Content( //TODO
	//            )
	//    )
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	//    @Produces(MediaType.)//TODO
	@PermitAll
	public Response getImageData(
		@Context SecurityContext securityContext
	) throws FileNotFoundException {
		logRequestContext(this.jwt, securityContext);
		log.info("Getting image data for {}", this.runByImage);
		
		if (!runByImage.exists() || !runByImage.isFile()) {
//			log.info("PWD: {}", System.getProperty("user.dir"));
			log.info("Image isFile? {}  Exists? {}", runByImage.isFile(), runByImage.exists());
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		//TODO:: restrict to only image types
		
		return Response.status(Response.Status.OK)
					   .entity(new FileInputStream(runByImage))
					   .type("image/" +FilenameUtils.getExtension(runByImage.getName()))
					   .build();
	}
	
}
