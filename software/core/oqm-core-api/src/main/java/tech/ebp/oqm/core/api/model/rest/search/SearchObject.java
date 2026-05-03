package tech.ebp.oqm.core.api.model.rest.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.service.mongo.search.PagingOptions;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;
import tech.ebp.oqm.core.api.service.mongo.search.SortType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

@ToString
@Getter
@Setter
public class SearchObject<T extends MainObject> {
	
	protected static boolean hasValue(String val){
		return val != null && !val.isBlank();
	}
	
	protected static boolean hasValue(Boolean val){
		return val != null;
	}
	
	protected static boolean hasValue(ObjectId val){
		return val != null;
	}
	
	protected static boolean hasValue(Enum<?> val){
		return val != null;
	}
	
	protected static boolean hasValue(Collection<?> val){
		return val != null && !val.isEmpty();
	}
	
	
	//paging
	@Parameter(description = "The number of results to return per page.")
	@QueryParam("pageSize") Integer pageSize;
	
	@Parameter(description = "The page number to return.")
	@QueryParam("pageNum") Integer pageNum;
	//sorting
	
	@Parameter(description = "The field to sort by.")
	@QueryParam("sortBy") String sortField;
	
	@Parameter(description = "How to sort the results.")
	@QueryParam("sortType") SortType sortType;
  
	//id search
	/**
	 * IDs of specific objects to search for.
	 */
	@Parameter(description = "IDs of specific objects to search for.")
	@QueryParam("id") List<ObjectId> objectIds;

	@JsonIgnore
	public Bson getSortBson(){
		return SearchUtils.getSortBson(this.sortField, this.sortType);
	}

	@JsonIgnore
	public PagingOptions getPagingOptions(){
		return PagingOptions.from(this);
	}
 
	@JsonIgnore
	public List<Bson> getSearchFilters(){
		List<Bson> filters = new ArrayList<>();
		
		if (hasValue(this.objectIds)) {
			List<Bson> idFilters = new ArrayList<>();
			for(ObjectId curId : this.objectIds) {
				idFilters.add(eq("_id", curId));
			}
			filters.add(or(idFilters));
		}
		return filters;
	}
}
