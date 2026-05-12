package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InteractingEntitySearch;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/interacting-entity")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class InteractingEntityPassthrough extends PassthroughProvider {
	
	
	@Getter
	@Inject
	@Location("tags/interactingEntityRef.html")
	Template historyTemplate;
	
	@GET
	public Uni<Response> searchEntities(
		@BeanParam InteractingEntitySearch search
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().interactingEntitySearch(this.getBearerHeaderStr(), search)
		);
	}
	
	@GET
	@Path("/{id}")
	public Uni<Response> getInteractingEntity(
		@PathParam("id") String id
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().interactingEntityGet(this.getBearerHeaderStr(), id)
		);
	}
	
	@GET
	@Path("/{id}/reference")
	public Uni<Response> getInteractingEntityReference(
		@PathParam("id") String id,
		@HeaderParam("Accept") String acceptType
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().interactingEntityGetReference(this.getBearerHeaderStr(), id)
				.map((ObjectNode reference)->{
					if (MediaType.TEXT_HTML.equalsIgnoreCase(acceptType)) {
						return Response.ok(
							historyTemplate.data("entityRef", reference)
								.data("rootPrefix", this.getRootPrefix())
						).build();
					}
					return Response.ok(reference).build();
				})
		);
	}
	
}
