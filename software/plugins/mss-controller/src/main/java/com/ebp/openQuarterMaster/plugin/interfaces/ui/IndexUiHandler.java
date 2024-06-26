package com.ebp.openQuarterMaster.plugin.interfaces.ui;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static java.util.Objects.requireNonNull;

@Path("/")
@Tags({@Tag(name = "UI", description = "Endpoints for web UI.")})
public class IndexUiHandler extends UiHandler {
    
    @Inject
    @Location("pages/index")
    Template page;
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @PermitAll
    public Response get() {
        if(this.getUserToken() != null){
            return Response.seeOther(
                UriBuilder.fromUri("/main").build()
            ).build();
        }
        
        return Response.ok()
                   .type(MediaType.TEXT_HTML_TYPE)
                   .entity(this.page.instance())
                   .build();
    }
    
}
