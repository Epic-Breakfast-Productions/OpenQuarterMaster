package tech.ebp.oqm.plugin.alertMessenger.connections;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.plugin.alertMessenger.utils.JwtUtils;

import java.util.List;

@Path("/api/connection-details")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class ConnectionDetailsInterface {

    @Inject
    ConnectionDetailsService service;

    @Inject
    JsonWebToken jwt;

    @GET
    public List<ConnectionDetails> getAll(
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("20") int size) {
        return service.getAll(JwtUtils.getId(jwt), JwtUtils.isAdmin(jwt), page, size);
    }

    @GET
    @Path("/{id}")
    public ConnectionDetails get(@PathParam("id") String id) {
        return service.get(id, JwtUtils.getId(jwt), JwtUtils.isAdmin(jwt));
    }

    @POST
    public ConnectionDetails create(ConnectionDetails details) {
        return service.create(details, JwtUtils.getId(jwt));
    }

    @PUT
    @Path("/{id}")
    public ConnectionDetails update(@PathParam("id") String id, ConnectionDetails details) {
        return service.update(id, details, JwtUtils.getId(jwt), JwtUtils.isAdmin(jwt));
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id) {
        service.delete(id, JwtUtils.getId(jwt), JwtUtils.isAdmin(jwt));
    }
}
