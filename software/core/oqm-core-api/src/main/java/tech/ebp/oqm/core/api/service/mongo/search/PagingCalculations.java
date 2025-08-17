package tech.ebp.oqm.core.api.service.mongo.search;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Iterator;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Setter(AccessLevel.PROTECTED)
@Schema(description = "Calculations on the search results related to paging.")
public class PagingCalculations {
	
	@Schema(required = true, description = "If this was the first page.", examples = {"false"})
	private boolean onFirstPage;
	
	@Schema(required = true, description = "If this was the last page.", examples = {"false"})
	private boolean onLastPage;
	
	@Schema(required = true, description = "The number of pages in the query.", examples = {"11"})
	private long numPages;
	
	@Schema(required = true, description = "The last page number.", examples = {"10"})
	private long lastPage;
	
	@Schema(required = true, description = "The current page number.", examples = {"1"})
	private long curPage;
	
	@Schema(required = true, description = "The next page number", examples = {"false"})
	private long nextPage;
	
	@Schema(required = true, description = "The previous page number.", examples = {"false"})
	private long previousPage;
	
	protected PagingCalculations(long curPageNum, long numPages) {
		this(
			curPageNum <= 1,
			curPageNum == numPages,
			numPages,
			numPages,
			curPageNum,
			(Math.min(curPageNum + 1, numPages)),
			(Math.max(curPageNum - 1, 1))
		);
	}
	
	public PagingCalculations(PagingOptions options, long numResults) {
		this(options.getPageNum(), (long) Math.ceil((double) numResults / (double) options.getPageSize()));
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
