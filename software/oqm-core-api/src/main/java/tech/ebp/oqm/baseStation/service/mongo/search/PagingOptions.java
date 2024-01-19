package tech.ebp.oqm.baseStation.service.mongo.search;

import lombok.Data;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;

/**
 * Object to describe paging options.
 */
@Data
public class PagingOptions {
	
	public static final int DEFAULT_PAGE_SIZE = 25;
	public static final int DEFAULT_PAGE_NUM = 1;
	
	
	public static PagingOptions from(SearchObject<?> searchObject, boolean defaultPageSizeIfNotSet){
		return from(searchObject.getPageSize(), searchObject.getPageNum(), defaultPageSizeIfNotSet, searchObject.getDefaultPageSize());
	}
	
	/**
	 * Gets paging options from those provided in query parameters.
	 *
	 * @param pageSize The size of the page
	 * @param pageNum The page number to get
	 *
	 * @return A paging options object. Can be null
	 */
	public static PagingOptions from(Integer pageSize, Integer pageNum, boolean defaultPageSizeIfNotSet, int defaultPageSize) {
		if (defaultPageSizeIfNotSet) {
			if (pageSize == null) {
				pageSize = defaultPageSize;
			}
		} else {
			if (pageSize == null && pageNum == null) {
				return new PagingOptions(Integer.MAX_VALUE, 1);
			}
			if (pageSize == null) {
				throw new IllegalArgumentException("Page size not provided.");
			}
		}
		if (pageNum == null) {
			pageNum = 1;
		}
		return new PagingOptions(pageSize, pageNum);
	}
	
	public static PagingOptions from(Integer pageSize, Integer pageNum, boolean defaultPageSizeIfNotSet) {
		return from(pageSize, pageNum, defaultPageSizeIfNotSet, DEFAULT_PAGE_SIZE);
	}
	
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
		this.pageSize = pageSize;
		this.pageNum = pageNum;
	}
	
	public int getSkipVal() {
		return this.pageSize * (this.pageNum - 1);
	}
	
}
