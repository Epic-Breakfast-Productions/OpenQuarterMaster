package com.ebp.openQuarterMaster.plugin.interfaces.rest;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.response.CommandResponse;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.service.VoiceSearchService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Path("/api/v1/voiceInteraction")
@Slf4j
@RequestScoped
public class VoiceInteraction {
	
	@Inject
	VoiceSearchService voiceSearchService;
	
	@GET
	@Path("/test")
	@RolesAllowed("inventoryView")
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectNode identifyModuleBlock(
	) throws IOException {
		return this.voiceSearchService.listenForIntent();
	}
}
