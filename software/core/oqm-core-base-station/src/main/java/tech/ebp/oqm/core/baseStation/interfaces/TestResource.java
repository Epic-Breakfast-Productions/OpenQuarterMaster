package tech.ebp.oqm.core.baseStation.interfaces;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.ebp.oqm.core.baseStation.utils.Roles;

@Path("/hello")
@RequestScoped
public class TestResource extends RestInterface {
    
    @GET
    @Path("authed")
    @RolesAllowed(Roles.INVENTORY_VIEW)
    @Produces(MediaType.TEXT_PLAIN)
    public String helloAuthed() {
        return "Hello user from RESTEasy Reactive";
    }
}
