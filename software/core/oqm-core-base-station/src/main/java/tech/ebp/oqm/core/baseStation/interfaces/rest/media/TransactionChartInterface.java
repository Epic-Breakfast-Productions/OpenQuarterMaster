package tech.ebp.oqm.core.baseStation.interfaces.rest.media;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;
import tech.ebp.oqm.core.baseStation.model.graph.GraphRequest;
import tech.ebp.oqm.core.baseStation.service.graph.GraphService;
import tech.ebp.oqm.core.baseStation.utils.Roles;

import java.io.IOException;

@Slf4j
@RolesAllowed(Roles.INVENTORY_VIEW)
@RequestScoped
@Path(ApiProvider.API_ROOT + "/media/charts/graph/")
public class TransactionChartInterface extends ApiProvider {

    @Inject
	GraphService provider;

    @GET
    @Path("transactions")
    @Produces("image/svg+xml")
    @Operation(summary = "Returns transaction history as SVG chart.")
    @APIResponse(responseCode = "200", description = "SVG chart generated.")
    public Response getTransactionCount(@BeanParam GraphRequest graphRequest) throws IOException {
        return Response.ok(provider.createGraphItemStock(
			this.getSelectedDb(),
			this.getBearerHeaderStr(),
			graphRequest
			))
            .type("image/svg+xml")
            .header("Content-Disposition", "attachment; filename=transactions.svg")
            .build();
    }
}
