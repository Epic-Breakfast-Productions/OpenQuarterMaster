package com.ebp.openQuarterMaster.baseStation.endpoints.media;

import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Validator;
import javax.ws.rs.Path;

@Traced
@Slf4j
@Path("/api/media/image")
@Tags({@Tag(name = "Media", description = "Endpoints media CRUD")})
@RequestScoped
public class ImageCrud extends EndpointProvider {
    @Inject
    JsonWebToken jwt;
    @Inject
    Validator validator;


}
