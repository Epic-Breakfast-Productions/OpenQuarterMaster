package com.ebp.openQuarterMaster.plugin.interfaces.rest;

import com.ebp.openQuarterMaster.plugin.interfaces.RestInterface;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.service.VoiceSearchService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import java.io.IOException;

@Path("/api/v1/voiceInteraction")
@Slf4j
@RequestScoped
@Tags({@Tag(name = "Voice Interaction", description = "Endpoints for interacting via voice")})
public class VoiceInteraction  extends RestInterface {
	
//	@Inject
//	VoiceSearchService voiceSearchService;
	
	@ConfigProperty(name = "voiceSearch.enabled")
	boolean enabled;
	
	private static Response getDisabledResponse(){
		return Response
				   .status(Response.Status.CONFLICT)
				   .entity("This feature is not currently enabled.")
				   .type(MediaType.TEXT_PLAIN_TYPE)
				   .build();
	}
	
	@GET
	@Path("/enabled")
	@RolesAllowed("inventoryView")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean checkEnabled(
	) {
		return this.enabled;
	}
	
//	@GET
//	@Path("/test")
//	@RolesAllowed("inventoryView")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response identifyModuleBlock(
//	) throws IOException {
//		if(!this.enabled){
//			return getDisabledResponse();
//		}
//		return Response.ok(this.voiceSearchService.listenForIntent()).build();
//	}
//
//	@GET
//	@Path("/itemSearch")
//	@RolesAllowed("inventoryView")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response itemSearchWithVoice(
//	) throws IOException {
//		if(!this.enabled){
//			return getDisabledResponse();
//		}
//		return Response.ok(this.voiceSearchService.searchForItems()).build();
//	}
}
