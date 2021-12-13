package com.ebp.openQuarterMaster.baseStation.service.mongo.search;

import lombok.*;

import java.util.Iterator;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PROTECTED)
public class PagingCalculations {
    private PagingOptions pagingOptions = new PagingOptions(PagingOptions.DEFAULT_PAGE_SIZE, PagingOptions.DEFAULT_PAGE_NUM);
    private int numResults;

    private boolean onFirstPage;
    private boolean onLastPage;
    private int numPages;
    private int lastPage;
    private int curPage;
    private int nextPage;
    private int previousPage;

    protected PagingCalculations(PagingOptions options, int numResults, int numPages){
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

    public PagingCalculations(PagingOptions options, int numResults){
        this(
                options,
                numResults,
                Math.max(1, numResults / options.getPageSize()) //Ciel?
        );
    }

    public PagingCalculations(PagingOptions options, SearchResult<?> searchResult){
        this(options, searchResult.getResults().size());
    }

    public boolean onPage(int curPage){
        return this.getCurPage() == curPage;
    }

    public Iterator<Integer> getPageIterator(){
        return new Iterator<>() {
            private final int end = getNumPages();
            private int current = 1;

            @Override
            public boolean hasNext() {
                return current <= end;
            }

            @Override
            public Integer next() {
                return current++;
            }
        };
    }

}
