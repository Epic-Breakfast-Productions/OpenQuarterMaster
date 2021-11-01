package com.ebp.openQuarterMaster.baseStation.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.data.pojos.User;
import com.ebp.openQuarterMaster.baseStation.data.pojos.UserGetResponse;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SortType;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.regex;

@Traced
@Slf4j
@Path("/user")
@Tags({@Tag(name = "Users")})
public class UserCrud {

    @Inject
    UserService service;

    @GET
    @Operation(
            summary = "Gets a list of users."
    )
    @APIResponse(
            responseCode = "200",
            description = "Users retrieved.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            type = SchemaType.ARRAY,
                            implementation = UserGetResponse.class
                    )
            ),
            headers = {
                    @Header(name="num-elements", description = "Gives the number of elements returned in the body."),
                    @Header(name="query-num-results", description = "Gives the number of results in the query given.")
            }
    )
    @APIResponse(
            responseCode = "204",
            description = "No items found from query given.",
            content = @Content(mediaType = "text/plain")
    )
    @Produces({MediaType.APPLICATION_JSON})
    public Response listInventoryItems(
            //for actual queries
            @QueryParam("name") String name,
            //paging
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("pageNum") Integer pageNum,
            //sorting
            @QueryParam("sortBy") String sortField,
            @QueryParam("sortType") SortType sortType
    ) {
        log.info("Searching for items with: ");

        List<Bson> filters = new ArrayList<>();
        Bson sort = SearchUtils.getSortBson(sortField, sortType);
        PagingOptions pageOptions = PagingOptions.fromQueryParams(pageSize, pageNum);

        if(name != null && !name.isBlank()){
            //TODO:: handle first and last name properly
            filters.add(regex("firstName", SearchUtils.getSearchTermPattern(name)));
        }
        Bson filter = (filters.isEmpty() ? null : and(filters));

        List<User> users = this.service.list(
                filter,
                sort,
                pageOptions
        );
        if(users.isEmpty()){
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        List<UserGetResponse> output = users
                .stream()
                .map((User user)->{
                    return UserGetResponse.builder(user).build();
                })
                .collect(Collectors.toList());


        return Response
                .status(Response.Status.OK)
                .entity(output)
                .header("num-elements", output.size())
                .header("query-num-results", this.service.count(filter))
                .build();
    }

}
