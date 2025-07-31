package tech.ebp.oqm.core.api.interfaces.endpoints;

import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;

@NoArgsConstructor
public abstract class ObjectProvider extends EndpointProvider {
	
	protected Response.ResponseBuilder getSearchResultResponseBuilder(SearchResult<?> searchResult) {
		return Response.status(Response.Status.OK)
				   .entity(searchResult);
	}
	
}
