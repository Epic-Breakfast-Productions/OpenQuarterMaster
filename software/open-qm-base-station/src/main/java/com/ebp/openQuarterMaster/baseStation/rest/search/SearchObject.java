package com.ebp.openQuarterMaster.baseStation.rest.search;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SortType;
import com.ebp.openQuarterMaster.lib.core.MainObject;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;

import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
public abstract class SearchObject<T extends MainObject> {
	//paging
	@QueryParam("pageSize") Integer pageSize;
	@QueryParam("pageNum") Integer pageNum;
	//sorting
	@QueryParam("sortBy") String sortField;
	@QueryParam("sortType")
	SortType sortType;
	
	public Bson getSortBson(){
		return SearchUtils.getSortBson(this.sortField, this.sortType);
	}
	
	public PagingOptions getPagingOptions(boolean defaultPageSizeIfNotSet){
		return PagingOptions.fromQueryParams(this.pageSize, this.pageNum, defaultPageSizeIfNotSet);
	}
	
	public List<Bson> getSearchFilters(){
		return new ArrayList<>();
	}
}
