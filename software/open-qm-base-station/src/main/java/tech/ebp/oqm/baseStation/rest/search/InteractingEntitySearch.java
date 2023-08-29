package tech.ebp.oqm.baseStation.rest.search;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;

import java.util.List;

import static com.mongodb.client.model.Filters.regex;

@ToString(callSuper = true)
@Getter
public class InteractingEntitySearch extends SearchKeyAttObject<InteractingEntity> {
	@QueryParam("name") String name;
	//TODO:: object specific fields, add to bson filter list
	
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> output = super.getSearchFilters();
		
		if (name != null && !name.isBlank()) {
			//TODO:: handle first and last name properly
			output.add(regex("firstName", SearchUtils.getSearchTermPattern(name)));
		}
		
		return output;
	}
}
