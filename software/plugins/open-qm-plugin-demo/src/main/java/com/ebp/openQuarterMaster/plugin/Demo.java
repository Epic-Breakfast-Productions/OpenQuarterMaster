package com.ebp.openQuarterMaster.plugin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/demo")
public class Demo {

    @GET
    @Path("1")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello1() {
        return "Hello 1; " + UUID.randomUUID();
    }

    @GET
    @Path("2")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello2() {
        return "Hello 2; " + UUID.randomUUID();
    }
}