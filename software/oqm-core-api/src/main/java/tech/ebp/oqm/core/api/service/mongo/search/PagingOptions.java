package tech.ebp.oqm.core.api.service.mongo.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;

/**
 * Object to describe paging options.
 */
@Data
@AllArgsConstructor
public class PagingOptions {
	
	public static final int DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;
	public static final int DEFAULT_PAGE_NUM = 1;
	
	public static PagingOptions from(SearchObject<?> searchObject){
		return from(searchObject.getPageSize(), searchObject.getPageNum());
	}
	
	/**
	 * Gets paging options from those provided in query parameters.
	 *
	 * @param pageSize The size of the page
	 * @param pageNum The page number to get
	 *
	 * @return A paging options object. Can be null
	 */
	public static PagingOptions from(Integer pageSize, Integer pageNum) {
		if(pageSize == null){
			return new PagingOptions(false, DEFAULT_PAGE_SIZE, DEFAULT_PAGE_NUM);
		} else {
			if (pageNum == null) {
				pageNum = DEFAULT_PAGE_NUM;
			}
		}
		return new PagingOptions(pageSize, pageNum);
	}
	
	public final boolean doPaging;
	/** The size of the pages */
	public final int pageSize;
	/** The number of the page we are on */
	public final int pageNum;
	
	public PagingOptions(int pageSize, int pageNum) {
		if (pageSize < 1) {
			throw new IllegalArgumentException("Page size cannot be less than 1.");
		}
		if (pageNum < 1) {
			throw new IllegalArgumentException("Page number cannot be less than 1.");
		}
		this.doPaging = true;
		this.pageSize = pageSize;
		this.pageNum = pageNum;
	}
	
	public int getSkipVal() {
		return this.pageSize * (this.pageNum - 1);
	}
	
}
