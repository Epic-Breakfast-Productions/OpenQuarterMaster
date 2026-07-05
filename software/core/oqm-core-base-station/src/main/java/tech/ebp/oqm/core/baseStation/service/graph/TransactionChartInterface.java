package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.AppliedTransactionSearch;

@Slf4j
@RolesAllowed(Roles.INVENTORY_VIEW)
@RequestScoped
@Path(ApiProvider.API_ROOT + "/v1")
public class TransactionChartInterface {

    private final GraphProvider provider;

    public TransactionChartInterface(GraphProvider provider) {
        this.provider = provider;
    }

    // @GET
    // @Path("/db/{dbIdOrName}/inventory/item")
    // @Produces(MediaType.APPLICATION_JSON)
    // public int getCount(@PathParam("dbIdOrName") String dbIdOrName) {
    //     return Integer.parseInt(Response.ok().entity("Count for dbIdOrName: " + dbIdOrName).build().toString());
    // }

    @GET
    @Path("/graph/transactions/{dbIdOrName}/{itemId}")
    @Produces("image/svg+xml")
    @Operation(summary = "Returns transaction history as an SVG chart.")
    @APIResponse(responseCode = "200", description = "SVG chart generated.")
    public Response getTransactionCount(
        @PathParam("dbIdOrName") String dbIdOrName,
        @PathParam("itemId") String itemId
        //TODO: timestamps
        //if null, get tranactions for the last 30/90 days
        ) {
        return Response.ok(provider.getGraph(dbIdOrName, itemId, null, null))
            .type("image/svg+xml")
            .header("Content-Disposition", "inline; filename=transactions.svg")
            .build();
    }
}
