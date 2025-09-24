package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.Optional;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/identifier/unique/")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class UniqueIdPassthrough extends PassthroughProvider {
	
	
	@POST
	@Path("generator")
	@Operation(
		summary = "."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the code object."
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> newGenerator(
		ObjectNode newGenerator
	) {
		return this.handleCall(this.getOqmCoreApiClient().uniqueIdGeneratorAdd(this.getBearerHeaderStr(), this.getSelectedDb(), newGenerator));
	}
	
	@GET
	@Path("generator/{id}")
	@Operation(
		summary = "."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the code object."
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> getGenerator(
		@PathParam("id") String id
	) {
		return this.handleCall(this.getOqmCoreApiClient().uniqueIdGeneratorGet(this.getBearerHeaderStr(), this.getSelectedDb(), id));
	}
	
	@PUT
	@Path("generator/{id}")
	@Operation(
		summary = "."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the code object."
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> updateGenerator(
		@PathParam("id") String id,
		ObjectNode update
	) {
		return this.handleCall(this.getOqmCoreApiClient().uniqueIdGeneratorUpdate(this.getBearerHeaderStr(), this.getSelectedDb(), id, update));
	}
	
	@GET
	@Path("generator/{id}/generate")
	@Operation(
		summary = "."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the code object."
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> generate(
		@PathParam("id") String id,
		@QueryParam("num") Optional<Integer> num
	) {
		return this.handleCall(this.getOqmCoreApiClient().uniqueIdGenerate(this.getBearerHeaderStr(), this.getSelectedDb(), id, num.orElse(1)));
	}
	
}
