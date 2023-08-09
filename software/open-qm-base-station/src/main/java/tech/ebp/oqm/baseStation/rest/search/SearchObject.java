package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingOptions;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;
import tech.ebp.oqm.baseStation.service.mongo.search.SortType;

import jakarta.ws.rs.QueryParam;
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
	@QueryParam("sortType") SortType sortType;
	
	public Bson getSortBson(){
		return SearchUtils.getSortBson(this.sortField, this.sortType);
	}
	
	public PagingOptions getPagingOptions(boolean defaultPageSizeIfNotSet){
		return PagingOptions.from(this, defaultPageSizeIfNotSet);
	}
	
	public List<Bson> getSearchFilters(){
		return new ArrayList<>();
	}
	
	public int getDefaultPageSize(){
		return PagingOptions.DEFAULT_PAGE_SIZE;
	}
}
