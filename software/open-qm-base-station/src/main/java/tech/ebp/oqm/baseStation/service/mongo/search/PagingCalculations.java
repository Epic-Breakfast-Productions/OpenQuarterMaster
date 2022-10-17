package tech.ebp.oqm.baseStation.service.mongo.search;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Iterator;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Setter(AccessLevel.PROTECTED)
public class PagingCalculations {
	
	private PagingOptions pagingOptions = new PagingOptions(PagingOptions.DEFAULT_PAGE_SIZE, PagingOptions.DEFAULT_PAGE_NUM);
	private long numResults;
	
	private boolean onFirstPage;
	private boolean onLastPage;
	private long numPages;
	private long lastPage;
	private long curPage;
	private long nextPage;
	private long previousPage;
	
	protected PagingCalculations(PagingOptions options, long numResults, long numPages) {
		this(
			options,
			numResults,
			options.getPageNum() <= 1,
			options.getPageNum() == numPages,
			numPages,
			numPages,
			options.getPageNum(),
			(Math.min(options.getPageNum() + 1, numPages)),
			(Math.max(options.getPageNum() - 1, 1))
		);
	}
	
	public PagingCalculations(PagingOptions options, long numResults) {
		this(
			options,
			numResults,
			(long) Math.ceil((double) numResults / (double) options.getPageSize()) //Ciel?
		);
	}
	
	public PagingCalculations(SearchResult<?> searchResult) {
		this(searchResult.getPagingOptions(), searchResult.getNumResultsForEntireQuery());
	}
	
	public boolean onPage(long curPage) {
		return this.getCurPage() == curPage;
	}
	
	public boolean onPage(int curPage) {
		return this.onPage((long) curPage);
	}
	
	public Iterator<Long> getPageIterator() {
		return new Iterator<>() {
			private final long end = getNumPages();
			private long current = 1;
			
			@Override
			public boolean hasNext() {
				return current <= end;
			}
			
			@Override
			public Long next() {
				return current++;
			}
		};
	}
	
}
