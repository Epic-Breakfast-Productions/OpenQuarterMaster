package com.ebp.openQuarterMaster.driverServer.interfaces.rest;

import com.ebp.openQuarterMaster.lib.driver.ModuleState;
import com.ebp.openQuarterMaster.lib.driver.rest.PostMessageRequest;
import com.ebp.openQuarterMaster.driverServer.services.SerialService;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Singleton
@Slf4j
public class SerialManagerInterfaceDemo {
	@Inject
	SerialService serialService;
	
	@GET
	@Path("/getState")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<ModuleState> getState() throws InterruptedException {
		return this.serialService.getStateUni();
	}
	
	@POST
	@Path("/postMessage")
	@Consumes(MediaType.APPLICATION_JSON)
	public Uni<Void> postMessage(
		@Valid PostMessageRequest pmr
	) throws InterruptedException {
		return this.serialService.setMessageUni(pmr.getMessage());
	}
}
