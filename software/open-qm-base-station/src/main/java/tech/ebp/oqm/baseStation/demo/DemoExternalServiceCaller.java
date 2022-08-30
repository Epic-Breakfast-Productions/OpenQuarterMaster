package tech.ebp.oqm.baseStation.demo;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;

@Path("/demo")
@RegisterRestClient(configKey = "demoServiceExternal")
public interface DemoExternalServiceCaller {
	
	@GET
	@Path("1")
	@Produces(MediaType.TEXT_PLAIN)
	CompletionStage<String> get1(@HeaderParam("authorization") String id);
	//    String get1(@HeaderParam("authorization") String id);
	
	@GET
	@Path("2")
	@Produces(MediaType.TEXT_PLAIN)
	CompletionStage<String> get2(@HeaderParam("authorization") String id);
	//    String get2(@HeaderParam("authorization") String id);
        @GET
        @Path("recursive/{current}")
        @Produces(MediaType.TEXT_PLAIN)
        CompletionStage<String> recurse(
                @PathParam("current") int current
        );
	
}
