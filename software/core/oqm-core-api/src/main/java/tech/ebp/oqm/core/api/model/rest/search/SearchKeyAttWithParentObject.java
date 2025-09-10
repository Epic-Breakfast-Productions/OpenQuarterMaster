package tech.ebp.oqm.core.api.model.rest.search;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.model.object.HasParent;
import tech.ebp.oqm.core.api.service.mongo.HasParentObjService;
import tech.ebp.oqm.core.api.service.mongo.MongoObjectService;
import tech.ebp.oqm.core.api.service.mongo.MongoService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;

import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Filters.or;

@ToString(callSuper = true)
@Setter
@Getter
public abstract class SearchKeyAttWithParentObject<T extends AttKeywordMainObject & HasParent> extends SearchKeyAttObject<T> {
	
	@QueryParam("hasParent")
	Optional<Boolean> hasParent;
	
	@QueryParam("parent")
	ObjectId parent;
	
	@QueryParam("includeAllChildrenUnderParent")
	boolean includeAllChildrenUnderParent = false;
	
	@Override
	public List<Bson> getSearchFilters(MongoService<?, ?, ?> service) {
		List<Bson> filters = this.getSearchFilters(service);
		
		if(this.getHasParent().isPresent()){
			if(this.getHasParent().get()){
				filters.add(ne("parent", null));
			} else {
				filters.add(eq("parent", null));
			}
		}
		
		if(this.hasValue(this.getParent())){
			if(this.includeAllChildrenUnderParent){
				filters.add(
					or(
						((HasParentObjService<T, ?, ?, ?>)service).getAllChildren(this.getParent())
							.stream()
							.map(Filters::eq)
							.toList()
					)
				);
			} else {
				filters.add(eq("parent", this.getParent()));
			}
		}
		
		
		//TODO:: don't know if valid to add to list like this
//		filters.add(
//		Aggregates.graphLookup(
//			service.getCollectionName(),
//			"$_id",
//			"parent",
//			"_id",
//			"children"
//		)
//		);
		
		return filters;
	}
}
