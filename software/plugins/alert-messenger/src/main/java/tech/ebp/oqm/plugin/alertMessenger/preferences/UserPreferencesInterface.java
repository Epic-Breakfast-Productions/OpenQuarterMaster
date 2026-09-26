package tech.ebp.oqm.plugin.alertMessenger.preferences;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/api/user-preferences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserPreferencesInterface {

    @Inject
    UserPreferencesService service;

    @GET
    public List<UserPreferences> get(
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("20") int size) {
        return service.get(page, size);
    }

    @GET
    @Path("/{id}")
    public UserPreferences get(@PathParam("id") String id) {
        return service.get(id);
    }

    @POST
    public UserPreferences create(@Valid UserPreferences request, @HeaderParam("Authorization") JsonWebToken jwt) {
        return service.create(request, jwt);
    }

    @PUT
    public UserPreferences update(@Valid UserPreferences request, @HeaderParam("Authorization") JsonWebToken jwt) {
        return service.update(request, jwt);
    }

    @DELETE
    public void delete(@HeaderParam("Authorization") JsonWebToken jwt) {
        service.delete(jwt);
    }
}
