package tech.ebp.oqm.core.api.service.mongo.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "The paged search result from the given search.")
public class SearchResult<T> {
	
	@Schema(description = "The results in this page of search.")
	private List<T> results;
	
	@Schema(description = "The number of results in this the set or results.", examples = {"1"})
	private long numResults;
	
	@Schema(description = "The number of results in the entire query, including all pages.", examples = {"2"})
	private long numResultsForEntireQuery;
	
	@Schema(description = "If any real search query parameters were given.", examples = {"false"})
	private boolean hadSearchQuery;
	
	@Schema(description = "The paging options used to inform the search.")
	private PagingOptions pagingOptions;
	
	@Schema(description = "If any real search query parameters were given.")
	private PagingCalculations pagingCalculations;
	
	public SearchResult(List<T> results) {
		this(
			results,
			results.size(),
			results.size(),
			false,
			null,
			null
		);
	}
	
	public SearchResult(List<T> results, long numResultsForEntireQuery, boolean hadSearchQuery, PagingOptions pagingOptions) {
		this(
			results,
			results.size(),
			numResultsForEntireQuery,
			hadSearchQuery,
			pagingOptions,
			new PagingCalculations(pagingOptions, numResultsForEntireQuery)
		);
	}
	
	
	@Schema(required = true, description = "If this search result is empty.", examples = {"false"})
	public boolean isEmpty() {
		return this.results.isEmpty();
	}
}