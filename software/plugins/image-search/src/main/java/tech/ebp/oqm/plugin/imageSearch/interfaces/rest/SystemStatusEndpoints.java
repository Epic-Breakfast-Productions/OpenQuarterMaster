package tech.ebp.oqm.plugin.imageSearch.interfaces.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import tech.ebp.oqm.plugin.imageSearch.model.SystemStatus;
import tech.ebp.oqm.plugin.imageSearch.service.SystemStatusService;

@RequestScoped
@Path("/api/v1/status")
public class SystemStatusEndpoints {

	@Inject
	SystemStatusService systemStatusService;

	@GET
	public SystemStatus getStatus(){
		return this.systemStatusService.getStatus();
	}
}
