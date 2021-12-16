package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.restCalls.KeycloakServiceCaller;
import com.ebp.openQuarterMaster.baseStation.service.mongo.ImageService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.*;
import com.ebp.openQuarterMaster.lib.core.media.Image;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

import static com.ebp.openQuarterMaster.baseStation.ui.UiUtils.getLoadTimestamp;

@Traced
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class Images extends UiProvider {

    @Inject
    @Location("webui/pages/images")
    Template images;

    @Inject
    ImageService imageService;

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;
    @Inject
    @RestClient
    KeycloakServiceCaller ksc;


    @GET
    @Path("/images")
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    public Response images(
            @Context SecurityContext securityContext,
            @QueryParam("title") String imageTitle,
            //paging
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("pageNum") Integer pageNum,
            //sorting
            @QueryParam("sortBy") String sortField,
            @QueryParam("sortType") SortType sortType,
            @CookieParam("jwt_refresh") String refreshToken
    ) {
        logRequestContext(jwt, securityContext);
        User user = userService.getFromJwt(this.jwt);
        List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(refreshAuthToken(ksc, refreshToken));

        Bson sort = SearchUtils.getSortBson(sortField, sortType);
        PagingOptions pageOptions = PagingOptions.fromQueryParams(pageSize, pageNum, true);

        SearchResult<Image> searchResults = this.imageService.search(
                imageTitle,
                null,
                null,
                sort,
                pageOptions
        );

        Response.ResponseBuilder responseBuilder = Response.ok(
                images
                        .data("pageLoadTimestamp", getLoadTimestamp())
                        .data(USER_INFO_DATA_KEY, UserGetResponse.builder(user).build())
                        .data("showSearch", false)
                        .data("searchResult", searchResults)
                        .data("pagingCalculations", new PagingCalculations(pageOptions, searchResults)),
                MediaType.TEXT_HTML_TYPE
        );

        if (newCookies != null && !newCookies.isEmpty()) {
            responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
        }

        return responseBuilder.build();
    }
}
