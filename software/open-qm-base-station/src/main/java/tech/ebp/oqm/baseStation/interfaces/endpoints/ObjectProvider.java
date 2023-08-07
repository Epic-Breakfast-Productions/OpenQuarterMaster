package tech.ebp.oqm.baseStation.interfaces.endpoints;

import lombok.NoArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@NoArgsConstructor
public abstract class ObjectProvider extends EndpointProvider {
	
	protected Response.ResponseBuilder getSearchResultResponseBuilder(SearchResult<?> searchResult) {
		return Response.status(Response.Status.OK)
				   .entity(searchResult.getResults())
				   .header("num-elements", searchResult.getResults().size())
				   .header("query-num-results", searchResult.getNumResultsForEntireQuery());
	}
	
	protected ObjectProvider(
		JsonWebToken jwt,
		InteractingEntityService interactingEntityService,
		SecurityContext securityContext
	) {
		super(jwt, interactingEntityService, securityContext);
	}
}
