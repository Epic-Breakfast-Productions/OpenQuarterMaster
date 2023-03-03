package tech.ebp.oqm.baseStation.interfaces.endpoints.info;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/media/runBy")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@RequestScoped
public class RunByUtils extends EndpointProvider {
	
	@Inject
	JsonWebToken jwt;
	
	@ConfigProperty(name = "service.runBy.logo", defaultValue = "/")
	File runByLogo;
	@ConfigProperty(name = "service.runBy.banner", defaultValue = "/")
	File runByBanner;
	
	@WithSpan
	private static Response getImage(File image) throws FileNotFoundException {
		
		if (!image.exists() || !image.isFile()) {
			//			log.info("PWD: {}", System.getProperty("user.dir"));
			log.info("Image isFile? {}  Exists? {}", image.isFile(), image.exists());
			return Response.status(Response.Status.NOT_FOUND).entity("No valid file set for image.").build();
		}
		
		//TODO:: restrict to only image types, throw 500 if not valid
		
		return Response.status(Response.Status.OK)
					   .entity(new FileInputStream(image))
					   .type("image/" + FilenameUtils.getExtension(image.getName()))
					   .build();
	}
	
	@GET
	@Path("{image}")
	@Operation(
		summary = "Gets a particular image's data string for use in html images."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
		content = @Content(mediaType = "image/*")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given. No runBy image.",
		content = @Content(mediaType = "text/plain")
	)
	@PermitAll
	@Produces({"image/*", "text/plain"})
	public Response getImageData(
		@Context SecurityContext securityContext,
		@PathParam("image") String runByImage
	) throws FileNotFoundException {
		logRequestContext(this.jwt, securityContext);
		log.info("Getting image data for {}", runByImage);
		
		switch (runByImage) {
			case "logo":
				return getImage(this.runByLogo);
			case "banner":
				return getImage(this.runByBanner);
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
}
