package tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.network;

import io.quarkus.rest.client.reactive.Url;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;

@RegisterRestClient(configKey = "mss-module-rest-client")
public interface MssModuleRestClient {

	@POST
	@Path("/command")
	CommandResponse sendCommand(
		@Url String url, @HeaderParam("Authorization") String authorization, Command command
	);

}
