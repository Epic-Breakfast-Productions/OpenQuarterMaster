package com.ebp.openQuarterMaster.driverServer.interfaces.rest;

import com.ebp.openQuarterMaster.lib.driver.ModuleState;
import com.ebp.openQuarterMaster.lib.driver.rest.PostMessageRequest;
import com.ebp.openQuarterMaster.driverServer.services.SerialService;
import lombok.extern.slf4j.Slf4j;

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
public class SerialManagerInterfaceDemo {
	@Inject
	SerialService serialService;
	
	@GET
	@Path("/getState")
	@Produces(MediaType.APPLICATION_JSON)
	public ModuleState getState() throws InterruptedException {
		return this.serialService.getState();
	}
	
	@POST
	@Path("/postMessage")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postMessage(
		PostMessageRequest pmr
	) throws InterruptedException {
		return Response.ok(
			this.serialService.setMessage(pmr.getMessage())
		).build();
	}
}
