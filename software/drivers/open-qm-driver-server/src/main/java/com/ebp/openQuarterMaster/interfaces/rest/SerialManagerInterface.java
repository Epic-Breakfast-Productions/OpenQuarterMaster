package com.ebp.openQuarterMaster.interfaces.rest;

import com.ebp.openQuarterMaster.lib.driver.State;
import com.ebp.openQuarterMaster.lib.driver.rest.PostMessageRequest;
import com.ebp.openQuarterMaster.services.SerialService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fazecast.jSerialComm.SerialPort;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/")
@Singleton
@Slf4j
public class SerialManagerInterface {
	@Inject
	SerialService serialService;
	
	@GET
	@Path("/getState")
	@Produces(MediaType.APPLICATION_JSON)
	public State getState() throws InterruptedException, IOException {
		return this.serialService.getState();
	}
	
	@POST
	@Path("/postMessage")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postMessage(
		PostMessageRequest pmr
	) throws InterruptedException, IOException {
		return Response.ok(
			this.serialService.setMessage(pmr.getMessage())
		).build();
	}
}
