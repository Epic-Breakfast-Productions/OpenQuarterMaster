package tech.ebp.oqm.plugin.alertMessenger.preferences;

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
import jakarta.ws.rs.core.Response;

@Path("/api/user-preferences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserPreferencesInterface {

    @Inject
    UserPreferencesService service;

    @GET
    public Response get(
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("20") int size) {
        return Response.ok(service.get(page, size)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        return Response.ok(service.get(id)).build();
    }

    @POST
    public Response create(@Valid UserPreferences request) {
        UserPreferences created = service.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    } //TODO: get userId from token without passing it in the request body

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, @Valid UserPreferences request) {
        return Response.ok(service.update(id, request)).build();
    } //TODO: get userId from token without passing it in the request body

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        service.delete(id);
        return Response.noContent().build();
    } //TODO: get userId from token without passing it in the request body
}
