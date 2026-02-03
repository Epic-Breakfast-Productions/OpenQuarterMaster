package tech.ebp.oqm.core.api.model.rest.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.service.mongo.search.PagingOptions;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;
import tech.ebp.oqm.core.api.service.mongo.search.SortType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@ToString
@Getter
@Setter
public class SearchObject<T extends MainObject> {
	//paging
	@QueryParam("pageSize") Integer pageSize;
	@QueryParam("pageNum") Integer pageNum;
	//sorting
	@QueryParam("sortBy") String sortField;
	@QueryParam("sortType") SortType sortType;
  
	//id search
	@QueryParam("id") ObjectId objectId;

	public Bson getSortBson(){
		return SearchUtils.getSortBson(this.sortField, this.sortType);
	}

	public PagingOptions getPagingOptions(){
		return PagingOptions.from(this);
	}
  
	public List<Bson> getSearchFilters(){
		List<Bson> filters = new ArrayList<>();
		if (this.hasValue(this.objectId)) {
			filters.add(eq("_id", this.objectId));
		}
		return filters;
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
	
	protected boolean hasValue(Enum<?> val){
		return val != null;
	}
	
	protected boolean hasValue(Collection<?> val){
		return val != null && !val.isEmpty();
	}
}
