package com.ebp.openQuarterMaster.plugin.demo.quarkus;

import org.eclipse.microprofile.opentracing.Traced;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Traced
@Path("/demo")
public class Demo {
    
    @Inject
    ResponseCreator responseCreator;

    @GET
    @Path("1")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello1() {
        return responseCreator.getResponse(1);
    }

    @GET
    @Path("2")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello2() {
        return responseCreator.getResponse(2);
    }
    
    @GET
    @Path("recursive/{current}")
    @Produces(MediaType.TEXT_PLAIN)
    public String recurse(
        @HeaderParam("authorization") String id,
        @PathParam("current") int current
    ) {
        return responseCreator.getRecursiveResponse(current, id);
    }
    
}