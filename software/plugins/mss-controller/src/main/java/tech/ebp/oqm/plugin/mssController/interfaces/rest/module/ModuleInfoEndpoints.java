package tech.ebp.oqm.plugin.mssController.interfaces.rest.module;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.MssConnectorInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.service.mssConn.MssConnectionService;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.MssConnector;

import java.util.stream.Stream;

@Path("/module/")
public class ModuleInfoEndpoints {

	@Getter
	@Inject
	MssConnectionService mssConnectionService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Stream<MssConnectorInfo> getModules() {
		return this.getMssConnectionService().getConnectors()
				   .stream().map(MssConnector::toConnInfo);
	}

	@GET
	@Path("/{serialId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MssConnectorInfo getModule(@PathParam("serialId") String serialId) {
		return this.getMssConnectionService().getConnector(serialId)
				   .toConnInfo();
	}

	@GET
	@Path("/{serialId}/state")
	@Produces(MediaType.APPLICATION_JSON)
	public ModuleState getModuleState(@PathParam("serialId") String serialId) {
		return this.getMssConnectionService().getConnector(serialId)
				   .getLastModuleState();
	}

}
