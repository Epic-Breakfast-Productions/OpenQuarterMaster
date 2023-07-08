package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.history.EventType;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;

import javax.ws.rs.QueryParam;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

@ToString(callSuper = true)
@Getter
public class HistorySearch extends SearchObject<ObjectHistoryEvent> {
	
	@Setter
	@QueryParam("objectId")
	private ObjectId objectId;
	
	@QueryParam("eventType")
	private List<EventType> eventTypes;
	
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
		
		if (eventTypes != null && !this.getEventTypes().isEmpty()) {
			filters.add(
				or(
					this.getEventTypes().stream().map(
						(EventType t) ->eq("type", t)
					).toList()
				)
			);
		}
		
		
		return filters;
	}
}
