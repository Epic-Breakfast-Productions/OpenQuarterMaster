package com.ebp.openQuarterMaster.baseStation.service.mongo.search;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * Object to describe paging options.
 *
 * TODO:: test
 */
@Data
@Getter(AccessLevel.PRIVATE)
public class PagingOptions {
    /**
     * Gets paging options from those provided in query parameters.
     * @param pageSize The size of the page
     * @param pageNum The page number to get
     * @return A paging options object. Can be null
     */
    public static PagingOptions fromQueryParams(Integer pageSize, Integer pageNum){
        if(pageSize == null && pageNum == null){
            return null;
        }
        if(pageSize == null){
            throw new IllegalArgumentException("Page size not provided.");
        }
        if(pageNum == null){
            pageNum = 1;
        }
        return new PagingOptions(pageSize, pageNum);
    }

    /** The size of the pages */
    public final int pageSize;
    /** The number of the page we are on */
    public final int pageNum;

    public PagingOptions(int pageSize, int pageNum){
        if(pageSize < 1){
            throw new IllegalArgumentException("Page size cannot be less than 1.");
        }
        if(pageNum < 1){
            throw new IllegalArgumentException("Page number cannot be less than 1.");
        }
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }

    public int getSkipVal() {
        return this.pageSize*(this.pageNum - 1);
    }
}
