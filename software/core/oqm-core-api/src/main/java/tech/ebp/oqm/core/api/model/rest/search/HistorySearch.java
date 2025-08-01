package tech.ebp.oqm.core.api.model.rest.search;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;

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
		
		if(this.getObjectId() != null) {
			filters.add(
				eq(
					"objectId",
					this.getObjectId()
				)
			);
		}
		
		if (this.getEventTypes() != null && !this.getEventTypes().isEmpty()) {
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
