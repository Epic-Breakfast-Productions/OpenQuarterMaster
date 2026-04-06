package tech.ebp.oqm.core.api.service.mongo.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
	private int numPages;
	
	@Schema(required = true, description = "The size of the pages in the query.", examples = {"25"})
	private int pageSize;
	
	@Schema(required = true, description = "The last page number.", examples = {"10"})
	private int lastPage;
	
	@Schema(required = true, description = "The current page number.", examples = {"1"})
	private int curPage;
	
	@Schema(required = true, description = "The next page number", examples = {"false"})
	private int nextPage;
	
	@Schema(required = true, description = "The previous page number.", examples = {"false"})
	private int previousPage;
	
	@Schema(required = true, description = "The start of the page by index in the overall search.", examples = {"false"})
	private int pageResultIndexStart;
	
	@Schema(required = true, description = "The end of the page by index in the overall search.", examples = {"false"})
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
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isHasPages() {
		return getNumPages() > 1;
	}
	
	@JsonIgnore
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
