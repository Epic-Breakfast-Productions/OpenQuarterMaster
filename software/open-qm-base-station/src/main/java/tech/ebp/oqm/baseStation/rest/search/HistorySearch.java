package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@ToString(callSuper = true)
@Getter
public class HistorySearch extends SearchObject<ObjectHistoryEvent> {
	@Setter
	private ObjectId objectId;
	//TODO:: object specific fields, add to bson filter list
	//TODO:: Get hist in time range, etc
	
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		filters.add(
			eq(
				"objectId",
				this.getObjectId()
			)
		);
		
		return filters;
	}
}
