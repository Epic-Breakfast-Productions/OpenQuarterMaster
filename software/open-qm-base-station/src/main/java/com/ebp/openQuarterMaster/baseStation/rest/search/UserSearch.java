package com.ebp.openQuarterMaster.baseStation.rest.search;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.lib.core.object.user.User;

import javax.ws.rs.QueryParam;
import java.util.List;

import static com.mongodb.client.model.Filters.regex;

@ToString(callSuper = true)
@Getter
public class UserSearch extends SearchKeyAttObject<User> {
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
