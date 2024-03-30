package tech.ebp.oqm.baseStation.interfaces.endpoints.info;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/media/runBy")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@RequestScoped
public class RunByUtils extends EndpointProvider {
	
	@ConfigProperty(name = "service.runBy.logo", defaultValue = "/")
	File runByLogo;
	@ConfigProperty(name = "service.runBy.banner", defaultValue = "/")
	File runByBanner;
	
	//TODO:: contemplate caching this?
	private static Response getImage(File image) throws FileNotFoundException {
		
		if (!image.exists() || !image.isFile()) {
			//			log.info("PWD: {}", System.getProperty("user.dir"));
			log.info("Image isFile? {}  Exists? {}", image.isFile(), image.exists());
			return Response.status(Response.Status.NOT_FOUND).entity("No valid file set for image.").build();
		}
		
		//TODO:: restrict to only image types, throw 500 if not valid
		
		String imageExt = FilenameUtils.getExtension(image.getName());
		
		if(imageExt.toLowerCase().equals("svg")){
			imageExt = "svg+xml";
		}
		
		return Response.status(Response.Status.OK)
					   .entity(new FileInputStream(image))
					   .type("image/" + imageExt)
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
		@PathParam("image") String runByImage
	) throws FileNotFoundException {
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
