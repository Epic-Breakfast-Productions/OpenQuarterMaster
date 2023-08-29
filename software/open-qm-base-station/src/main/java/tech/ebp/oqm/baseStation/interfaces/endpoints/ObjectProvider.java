package tech.ebp.oqm.baseStation.interfaces.endpoints;

import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

@NoArgsConstructor
public abstract class ObjectProvider extends EndpointProvider {
	
	protected Response.ResponseBuilder getSearchResultResponseBuilder(SearchResult<?> searchResult) {
		return Response.status(Response.Status.OK)
				   .entity(searchResult.getResults())
				   .header("num-elements", searchResult.getResults().size())
				   .header("query-num-results", searchResult.getNumResultsForEntireQuery());
	}
	
}
