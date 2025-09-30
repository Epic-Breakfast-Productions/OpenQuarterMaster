package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.Optional;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/identifier/generator")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class IdGeneratorPassthrough extends PassthroughProvider {
	
	@POST
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
		return this.handleCall(this.getOqmCoreApiClient().idGeneratorAdd(this.getBearerHeaderStr(), this.getSelectedDb(), newGenerator));
	}
	
	@GET
	@Path("/{id}")
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
		return this.handleCall(this.getOqmCoreApiClient().idGeneratorGet(this.getBearerHeaderStr(), this.getSelectedDb(), id));
	}
	
	@PUT
	@Path("/{id}")
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
		return this.handleCall(this.getOqmCoreApiClient().idGeneratorUpdate(this.getBearerHeaderStr(), this.getSelectedDb(), id, update));
	}
	
	@GET
	@Path("/{id}/generate")
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
		return this.handleCall(this.getOqmCoreApiClient().idGenerate(this.getBearerHeaderStr(), this.getSelectedDb(), id, num.orElse(1)));
	}
	
	@GET
	@Path("barcode/{value}")
	@Operation(
		summary = "A barcode that represents the string given."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@Produces("image/svg+xml")
	public Uni<Response> getBarcode(
		@PathParam("value") String data
	) {
		return this.handleCall(
			this.getOqmCoreApiClient()
				.uniqueIdGetBarcodeImage(this.getBearerHeaderStr(), data)
				.map((String xmlData)->{
					return Response.status(Response.Status.OK)
							   .entity(xmlData)
							   .header("Content-Disposition", "attachment;filename=" + data + ".svg")
							   .type("image/svg+xml")
							   .build();
				})
		);
	}
	
}
