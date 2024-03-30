package tech.ebp.oqm.core.baseStation.interfaces.rest;

import io.quarkus.security.Authenticated;
import io.smallrye.config.PropertiesConfigSource;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.baseStation.interfaces.RestInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Class to handle getting the RunBy images, and other RunBy data in the future, if needed.
 */
@Slf4j
@Path("/media/runBy")
@Tags({@Tag(name = "UI")})
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class RunByEndpoints extends RestInterface {
	
	private static Response.ResponseBuilder runByLogoBuilder = null;
	private static Response.ResponseBuilder runByBannerBuilder = null;
	
	public enum RunByImage{
		logo,
		banner
	}
	
	private static Response.ResponseBuilder getImage(String configProperty) {
		try {
			log.info("Reading in runBy image from config: {}", configProperty);
			File image = ConfigProvider.getConfig().getValue(configProperty, File.class);
			log.debug("RunBy image location: {}", image);
			
			if (!image.exists() || !image.isFile()) {
				//			log.info("PWD: {}", System.getProperty("user.dir"));
				log.info("Image isFile? {}  Exists? {}", image.isFile(), image.exists());
				return Response.status(Response.Status.NOT_FOUND).entity("No valid file set for image.");
			}
			
			//TODO:: restrict to only image types, throw 500 if not valid
			
			String imageExt = FilenameUtils.getExtension(image.getName());
			
			if (imageExt.toLowerCase().equals("svg")) {
				imageExt = "svg+xml";
			}
			
			return Response.status(Response.Status.OK)
					   .entity(new FileInputStream(image))
					   .type("image/" + imageExt);
		} catch(Throwable e){
			log.error("Failed to read runBy image {}: ", configProperty, e);
			return Response.status(Response.Status.NOT_FOUND).entity("Failed to read in image file.");
		}
	}
	
	protected static synchronized Response.ResponseBuilder getLogoResponse(){
		if(runByLogoBuilder == null){
			runByLogoBuilder = getImage("service.runBy.logo");
		}
		return runByLogoBuilder;
	}
	protected static synchronized Response.ResponseBuilder getBannerResponse(){
		if(runByBannerBuilder == null){
			runByBannerBuilder = getImage("service.runBy.banner");
		}
		return runByBannerBuilder;
	}
	
	protected static Response getRunByImage(RunByImage imageToGet){
		Response.ResponseBuilder output = switch (imageToGet){
			case logo -> getLogoResponse();
			case banner -> getBannerResponse();
		};
		
		return output.build();
	}
	
	@GET
	@Path("{image}")
	@Operation(
		summary = "Gets a particular image's data string for use in html images. Will return a cached response after the first call."
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
		@PathParam("image") RunByImage runByImage
	) {
		log.info("Getting image data for {}", runByImage);
		
		return getRunByImage(runByImage);
	}
	
}
