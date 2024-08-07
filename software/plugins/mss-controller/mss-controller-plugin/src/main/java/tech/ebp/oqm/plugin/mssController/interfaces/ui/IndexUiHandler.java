package tech.ebp.oqm.plugin.mssController.interfaces.ui;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

@Path("/")
@Tags({@Tag(name = "UI", description = "Endpoints for web UI.")})
public class IndexUiHandler extends UiHandler {

    @GET
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    public Response index() throws MalformedURLException, URISyntaxException {
        return Response.seeOther(
            UriBuilder.fromUri("/main").build()
        ).build();
    }
}
