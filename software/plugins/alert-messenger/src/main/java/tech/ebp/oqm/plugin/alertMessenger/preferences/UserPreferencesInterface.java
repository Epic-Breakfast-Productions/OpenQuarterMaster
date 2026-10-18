package tech.ebp.oqm.plugin.alertMessenger.preferences;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.util.List;

@Path("/api/user-preferences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class UserPreferencesInterface {

    @Inject
    UserPreferencesService service;

    @Inject
    @RestClient
    OqmCoreApiClientService coreApiClient;

    @GET
    public List<UserPreferences> get(
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("20") int size, @Context HttpHeaders headers) {
        return service.getForUser(currentUserId(headers), page, size);
    }

    @GET
    @Path("/admin")
    @RolesAllowed({"admins", "inventoryAdmin", "realm-admin"})
    public List<UserPreferences> getAllForAdmin(
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("20") int size) {
        return service.getAll(page, size);
    }

    @GET
    @Path("/{id}")
    public UserPreferences get(@PathParam("id") String id, @Context HttpHeaders headers) {
        return service.get(id, currentUserId(headers));
    }

    @POST
    public UserPreferences create(@Valid UserPreferences request, @Context HttpHeaders headers) {
        return service.create(request, currentUserId(headers));
    }

    @PUT
    @Path("/{id}")
    public UserPreferences update(@PathParam("id") String id, @Valid UserPreferences request, @Context HttpHeaders headers) {
        return service.update(request, id, currentUserId(headers));
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id, @Context HttpHeaders headers) {
        service.deleteById(id, currentUserId(headers));
    }

    private String currentUserId(HttpHeaders headers) {
        return coreApiClient.interactingEntityGetSelf(headers.getHeaderString(HttpHeaders.AUTHORIZATION))
            .await().indefinitely().getId();
    }

}
