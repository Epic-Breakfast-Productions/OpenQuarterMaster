package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.Map;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/manage/db")
@Authenticated
@RequestScoped
public class DbManagementPassthrough extends PassthroughProvider {
	
	@Blocking
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> addDb(
		ObjectNode newDb
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().manageDbAdd(this.getBearerHeaderStr(), newDb)
				.eventually(()->{
					return Uni.createFrom().item(()->{
						getOqmDatabaseService().refreshCache();
						return null;
					}).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
				})
		);
	}
	
	@Blocking
	@DELETE
	@Path("/clear/{oqmDbIdOrName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> clearDatabase(
		@PathParam("oqmDbIdOrName")
		String oqmDbIdOrName
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().manageDbClear(this.getBearerHeaderStr(), oqmDbIdOrName)
		);
	}
	
	@Blocking
	@DELETE
	@Path("/clearAllDbs")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> clearAllDatabase() {
		log.info("Clearing dbs");
		return this.handleCall(
			this.getOqmCoreApiClient().manageDbClearAll(this.getBearerHeaderStr())
		);
	}
	
}
