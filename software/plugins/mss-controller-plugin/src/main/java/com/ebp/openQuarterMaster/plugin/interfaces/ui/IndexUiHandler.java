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

import static java.util.Objects.requireNonNull;

@Path("/")
public class IndexUiHandler {
    
    @Inject
    @Location("pages/index")
    Template page;
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @PermitAll
    public TemplateInstance get() {
        return page.instance();
    }
    
}
