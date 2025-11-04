package tech.ebp.oqm.core.api.service.mongo.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult<T> {
	
	private List<T> results;
	private long numResults;
	private long numResultsForEntireQuery;
	private boolean hadSearchQuery;
	private PagingOptions pagingOptions;
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
	
	public boolean isEmpty() {
		return this.results.isEmpty();
	}
	
	public boolean isHasPages() {
		return this.pagingCalculations.isHasPages();
	}
}