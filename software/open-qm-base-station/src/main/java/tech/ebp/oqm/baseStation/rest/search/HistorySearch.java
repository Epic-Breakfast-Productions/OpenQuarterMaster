package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;

@ToString(callSuper = true)
@Getter
public class HistorySearch extends SearchObject<ObjectHistoryEvent> {
	//TODO:: object specific fields, add to bson filter list
	//TODO:: Get hist in time range, etc
}
