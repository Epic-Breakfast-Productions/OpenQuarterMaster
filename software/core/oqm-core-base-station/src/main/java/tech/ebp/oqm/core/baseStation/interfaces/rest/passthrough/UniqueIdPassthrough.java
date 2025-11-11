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
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/identifier/unique")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class UniqueIdPassthrough extends PassthroughProvider {
	
	@GET
	@Path("barcode/{value}/{label}")
	@Operation(
		summary = "A barcode that represents the string given."
	)
	@APIResponse(
		responseCode = "200"
	)
	@Produces("image/svg+xml")
	public Uni<Response> getBarcode(
		@PathParam("value") String data,
		@PathParam("label") String label
	) {
		return this.handleCall(
			this.getOqmCoreApiClient()
				.uniqueIdGetBarcodeImage(this.getBearerHeaderStr(), data, label)
				.map((String xmlData)->{
					return Response.status(Response.Status.OK)
							   .entity(xmlData)
							   .header("Content-Disposition", "attachment;filename=" + data + "-" + label +".svg")
							   .type("image/svg+xml")
							   .build();
				})
		);
	}
	
}
