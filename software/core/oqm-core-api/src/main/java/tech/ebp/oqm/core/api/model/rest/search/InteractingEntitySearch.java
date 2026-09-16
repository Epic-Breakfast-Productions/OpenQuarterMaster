package tech.ebp.oqm.core.api.model.rest.search;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.regex;

@ToString(callSuper = true)
@Setter
@Getter
public class InteractingEntitySearch extends SearchKeyAttObject<InteractingEntity> {
	@QueryParam("name") String name;
	@QueryParam("type") List<InteractingEntityType> types;
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> output = super.getSearchFilters();
		
		if (name != null && !name.isBlank()) {
			output.add(regex("name", SearchUtils.getSearchTermPattern(name)));
		}
		if(types != null && !types.isEmpty()) {
			output.add(
				or(
					types.stream().map(type->eq("type", type)).toList()
				)
			);
		}
		
		return output;
	}
}
