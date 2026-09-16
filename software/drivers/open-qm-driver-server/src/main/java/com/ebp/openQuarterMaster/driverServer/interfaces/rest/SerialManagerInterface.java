package com.ebp.openQuarterMaster.driverServer.interfaces.rest;

import com.ebp.openQuarterMaster.lib.driver.ModuleInfo;
import com.ebp.openQuarterMaster.lib.driver.ModuleState;
import com.ebp.openQuarterMaster.lib.driver.rest.PostMessageRequest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/modules")
public class SerialManagerInterface {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ModuleInfo> getModules() {
		//TODO:: get all modules we know/ have known about
		//TODO:: filtering
		//		return this.serialService.getState();
		return null;
	}
	
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> clean() {
		//TODO:: Cleans all non-active modules from the database, returning their ids
		return null;
	}
	
	@GET
	@Path("{moduleId}")
	@Produces(MediaType.APPLICATION_JSON)
	public ModuleInfo getModule(
		@PathParam("moduleId") String moduleId
	) {
		//TODO:: get a specific module we know about
		//TODO:: filtering
		//		return this.serialService.getState();
		return null;
	}
	
	@POST
	@Path("{moduleId}/postMessage")
	@Produces(MediaType.APPLICATION_JSON)
	public void postMess(
		@PathParam("moduleId") String moduleId,
		PostMessageRequest messageRequest
	) {
		//TODO:: get a specific module we know about
		//TODO:: filtering
		//		return this.serialService.getState();
	}
	
	@GET
	@Path("{moduleId}/state")
	@Produces(MediaType.APPLICATION_JSON)
	public ModuleState getState(
		@PathParam("moduleId") String moduleId
	) {
		//TODO:: get a specific module we know about
		//TODO:: filtering
		//		return this.serialService.getState();
		return null;
	}
	
	@DELETE
	@Path("{moduleId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> clean(
		@PathParam("moduleId") String moduleId
	) {
		//TODO:: Removes a particular module from the set held.
		return null;
	}
	
	
	
	
}
