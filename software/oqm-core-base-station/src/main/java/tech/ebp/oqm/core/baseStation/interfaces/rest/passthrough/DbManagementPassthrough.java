package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/manage/db")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class DbManagementPassthrough extends PassthroughProvider {

	@POST
	public Uni<String> getInteractingEntityReference(
		ObjectNode newDb
	) {
		return this.getOqmCoreApiClient().manageDbAdd(this.getBearerHeaderStr(), newDb)
			.eventually(()->{
				//TODO:: this correctly
				return Uni.createFrom().completionStage(()->{
					getOqmDatabaseService().refreshCache();
					return null;
				});
			});
	}
	
}
