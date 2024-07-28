package tech.ebp.oqm.plugin.mssController.interfaces.rest;

import tech.ebp.oqm.plugin.mssController.interfaces.RestInterface;
import tech.ebp.oqm.plugin.mssController.moduleInteraction.ModuleMaster;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.HighlightBlocksCommand;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.response.CommandResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("/api/v1/module/{moduleSerialId}")
@RequestScoped
@Tags({@Tag(name = "Module Interaction", description = "Endpoints for interacting with modules")})
public class ModuleInteraction extends RestInterface {
    
    @Inject
    ModuleMaster master;
    
    @GET
    @Path("identify")
    @RolesAllowed("inventoryView")
    @Produces(MediaType.APPLICATION_JSON)
    public CommandResponse identifyModule(
        @PathParam("moduleSerialId") String moduleSerialId
    ) {
        return this.master.getModule(moduleSerialId).sendModuleIdentifyCommand();
    }
    
    @GET
    @Path("/{blockNum}/identify")
    @RolesAllowed("inventoryView")
    @Produces(MediaType.APPLICATION_JSON)
    public CommandResponse identifyModuleBlock(
        @PathParam("moduleSerialId") String moduleSerialId,
        @PathParam("blockNum") Integer blockNum
    ) {
        return this.master.getModule(moduleSerialId).sendModuleBlockIdentifyCommand(blockNum);
    }
    
    @POST
    @Path("highlightBlocks")
    @RolesAllowed("inventoryView")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CommandResponse highlightBlocks(
        @PathParam("moduleSerialId") String moduleSerialId,
        @RequestBody HighlightBlocksCommand highlightBlocksCommand
    ) {
        return this.master.getModule(moduleSerialId).sendBlockHighlightCommand(highlightBlocksCommand);
    }
}
