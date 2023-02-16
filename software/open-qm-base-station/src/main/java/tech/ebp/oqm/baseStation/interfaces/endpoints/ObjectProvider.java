package tech.ebp.oqm.baseStation.interfaces.endpoints;

import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

import javax.ws.rs.core.Response;

public abstract class ObjectProvider extends EndpointProvider {
	
	protected Response.ResponseBuilder getSearchResultResponseBuilder(SearchResult<?> searchResult) {
		return Response.status(Response.Status.OK)
				   .entity(searchResult.getResults())
				   .header("num-elements", searchResult.getResults().size())
				   .header("query-num-results", searchResult.getNumResultsForEntireQuery());
	}
}
