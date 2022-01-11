package com.ebp.openQuarterMaster.baseStation.service.mongo.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult<T> {
    private List<T> results;
    private long numResultsForEntireQuery;
    private boolean hadSearchQuery;
}