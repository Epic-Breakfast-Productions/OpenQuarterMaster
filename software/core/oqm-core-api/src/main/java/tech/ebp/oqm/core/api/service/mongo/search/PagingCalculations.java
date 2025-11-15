package tech.ebp.oqm.core.api.service.mongo.search;

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
	private boolean onFirstPage;
	private boolean onLastPage;
	private int numPages;
	private int pageSize;
	private int lastPage;
	private int curPage;
	private int nextPage;
	private int previousPage;
	private int pageResultIndexStart;
	private int pageResultIndexEnd;
	
	protected PagingCalculations(int curPageNum, int numPages, int pageSize, int startIndex, int endIndex) {
		this(
			curPageNum <= 1,
			curPageNum == numPages,
			numPages,
			pageSize,
			numPages,
			curPageNum,
			(Math.min(curPageNum + 1, numPages)),
			(Math.max(curPageNum - 1, 1)),
			startIndex,
			endIndex
		);
	}
	
	public PagingCalculations(PagingOptions options, int numResults) {
		this(
			options.getPageNum(),
			(int) Math.ceil((double) numResults / (double) options.getPageSize()),
			options.getPageSize(),
			options.getSkipVal(),
			options.getSkipVal() + options.getPageSize() - 1
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
	
	public boolean isHasPages() {
		return getNumPages() > 1;
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
