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
	private long numPages;
	private long lastPage;
	private long curPage;
	private long nextPage;
	private long previousPage;
	private long pageResultIndexStart;
	private long pageResultIndexEnd;
	
	protected PagingCalculations(long curPageNum, long numPages, long startIndex, long endIndex) {
		this(
			curPageNum <= 1,
			curPageNum == numPages,
			numPages,
			numPages,
			curPageNum,
			(Math.min(curPageNum + 1, numPages)),
			(Math.max(curPageNum - 1, 1)),
			startIndex,
			endIndex
		);
	}
	
	public PagingCalculations(PagingOptions options, long numResults) {
		this(
			options.getPageNum(),
			(long) Math.ceil((double) numResults / (double) options.getPageSize()),
			options.getSkipVal(),
			options.getSkipVal() + options.pageSize - 1
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
