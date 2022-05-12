package com.ebp.openQuarterMaster.plugin;

import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;

@Traced
@Path("/demo")
@RegisterRestClient(configKey = "demoService")
public interface DemoServiceCaller {
	
	@GET
	@Path("recursive/{current}")
	@Produces(MediaType.TEXT_PLAIN)
	String recurse(
		@HeaderParam("authorization") String id,
		@PathParam("current") int current
	);
}
