package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SortType;
import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.storage.StorageBlock;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Traced
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class Storage extends UiProvider {

    @Inject
    @Location("webui/pages/storage")
    Template storage;

    @Inject
    UserService userService;

    @Inject
    StorageBlockService storageBlockService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("storage")
    @RolesAllowed("user")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance storage(
            @Context SecurityContext securityContext,
            //for actual queries
            @QueryParam("label") String label,
            @QueryParam("location") String location,
            @QueryParam("parents") List<String> parents,
            @QueryParam("keywords") List<String> keywords,
            @QueryParam("storedType") StoredType storedType,
            //paging
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("pageNum") Integer pageNum,
            //sorting
            @QueryParam("sortBy") String sortField,
            @QueryParam("sortType") SortType sortType
    ) {
        logRequestContext(jwt, securityContext);
        User user = userService.getFromJwt(this.jwt);

        Bson sort = SearchUtils.getSortBson(sortField, sortType);
        PagingOptions pageOptions = PagingOptions.fromQueryParams(pageSize, pageNum, false);

        SearchResult<StorageBlock> searchResults = this.storageBlockService.search(
                label,
                location,
                parents,
                keywords,
                sort,
                pageOptions
        );

        return storage
                .data("allowedUnitsMap", UnitUtils.ALLOWED_UNITS_MAP)
                .data(USER_INFO_DATA_KEY, UserGetResponse.builder(user).build())
                .data("showSearch", false)
                .data("numStorageBlocks", storageBlockService.count())
                .data("searchResult", searchResults)
                .data("storageService", storageBlockService);
    }

}
