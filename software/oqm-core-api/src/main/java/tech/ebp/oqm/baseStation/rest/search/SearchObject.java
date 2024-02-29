package tech.ebp.oqm.baseStation.rest.search;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingOptions;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;
import tech.ebp.oqm.baseStation.service.mongo.search.SortType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ToString
@Getter
@Setter
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
	
	public PagingOptions getPagingOptions(){
		return PagingOptions.from(this);
	}
	
	public List<Bson> getSearchFilters(){
		return new ArrayList<>();
	}
	
	protected boolean hasValue(String val){
		return val != null && !val.isBlank();
	}
	
	protected boolean hasValue(Boolean val){
		return val != null;
	}
	
	protected boolean hasValue(ObjectId val){
		return val != null;
	}
	
	protected boolean hasValue(Collection<?> val){
		return val != null && !val.isEmpty();
	}
}
