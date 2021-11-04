package com.ebp.openQuarterMaster.baseStation.endpoints.inventory.storage;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;

@Traced
@Slf4j
@Path("/storage")
@Tags({@Tag(name = "Storage", description = "Endpoints for managing Storage Mediums.")})
@RequestScoped
public class StorageCrud {


}
