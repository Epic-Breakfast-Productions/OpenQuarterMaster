package com.ebp.openQuarterMaster.plugin.interfaces.ui;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.ModuleMaster;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static java.util.Objects.requireNonNull;

@Path("/main")
@Tags({@Tag(name = "UI", description = "Endpoints for web UI.")})
public class MainUiHandler {
    
    @Inject
    @Location("pages/main")
    Template page;
    
    @Inject
    ModuleMaster moduleMaster;
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed("inventoryView")
    public TemplateInstance get() {
        return page.data("moduleMaster", moduleMaster);
    }
    
}
