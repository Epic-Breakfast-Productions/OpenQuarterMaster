package tech.ebp.oqm.core.api.service.mongo.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;

/**
 * Object to describe paging options.
 */
@Data
@AllArgsConstructor
@Schema(description = "Options to inform paging behavior.")
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
	
	/**
	 * If paging is to be done.
	 */
	@Schema(required = true, description = "If we are to do paging for the request.", examples = {"true"})
	public final boolean doPaging;
	
	/** The size of the pages */
	@Schema(required = true, description = "The size of the pages.", examples = {"25"})
	public final int pageSize;
	
	/** The number of the page we are on */
	@Schema(required = true, description = "The page to retrieve.", examples = {"1"})
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
