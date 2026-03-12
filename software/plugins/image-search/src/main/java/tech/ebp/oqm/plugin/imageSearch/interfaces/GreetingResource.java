package tech.ebp.oqm.plugin.imageSearch.interfaces;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
//        return System.getProperty("user.dir") + " <---- there!";
        return getClass().getClassLoader().getResource("testImages").toString();
    }
}
