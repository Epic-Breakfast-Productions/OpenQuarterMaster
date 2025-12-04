package tech.ebp.oqm.core.api.service.mongo.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult<T extends MainObject> {
	
	private List<T> results;
	private int numResults;
	private int numResultsForEntireQuery;
	private boolean hadSearchQuery;
	private PagingOptions pagingOptions;
	private PagingCalculations pagingCalculations;
	private SearchObject<?> searchObject;
	
	public SearchResult(List<T> results) {
		this(
			results,
			results.size(),
			results.size(),
			false,
			null,
			null,
			null
		);
	}
	
	public SearchResult(List<T> results, int numResultsForEntireQuery, boolean hadSearchQuery, PagingOptions pagingOptions, SearchObject<?> searchObject) {
		this(
			results,
			results.size(),
			numResultsForEntireQuery,
			hadSearchQuery,
			pagingOptions,
			new PagingCalculations(pagingOptions, numResultsForEntireQuery),
			searchObject
		);
	}
	
	public boolean isEmpty() {
		return this.results.isEmpty();
	}
	
	public boolean isHasPages() {
		return this.pagingCalculations.isHasPages();
	}
}