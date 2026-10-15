package tech.ebp.oqm.plugin.alertMessenger.preferences;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.plugin.alertMessenger.utils.JwtUtils;

import java.util.List;

@Path("/api/user-preferences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class UserPreferencesInterface {

    @Inject
    UserPreferencesService service;

    @Inject
    JsonWebToken jwt;

    @Inject
    @RestClient
    OqmCoreApiClientService coreApiClient;

    @GET
    public List<UserPreferences> get(
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("20") int size, @Context HttpHeaders headers) {
        return service.get(currentUserId(headers), JwtUtils.isAdmin(jwt), page, size);
    }

    @GET
    @Path("/{id}")
    public UserPreferences get(@PathParam("id") String id, @Context HttpHeaders headers) {
        return service.get(id, currentUserId(headers), JwtUtils.isAdmin(jwt));
    }

    @GET
    @Path("/user/{id}")
    public List<UserPreferences> getByUserId(@PathParam("id") String id, @Context HttpHeaders headers) {
        return service.getByUserId(id, currentUserId(headers), JwtUtils.isAdmin(jwt));
    }

    @POST
    public UserPreferences create(@Valid UserPreferences request, @Context HttpHeaders headers) {
        return service.create(request, currentUserId(headers));
    }

    @PUT
    @Path("/{id}")
    public UserPreferences update(@PathParam("id") String id, @Valid UserPreferences request, @Context HttpHeaders headers) {
        return service.update(request, id, currentUserId(headers), JwtUtils.isAdmin(jwt));
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id, @Context HttpHeaders headers) {
        service.deleteById(id, currentUserId(headers), JwtUtils.isAdmin(jwt));
    }

    private String currentUserId(HttpHeaders headers) {
        return coreApiClient.interactingEntityGetSelf(headers.getHeaderString(HttpHeaders.AUTHORIZATION))
            .await().indefinitely().getId();
    }
}
