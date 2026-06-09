package tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * Filter utilities for history events.
 */
public class HistoryEventFilter {

	/**
	 * A filter method accepting a history event and filter options.
	 * @param event The event to filter.
	 * @param filterOptions The options to filter by.
	 * @return If the event passes the filter.
	 */
	public static boolean filter(
		EventNotificationWrapper event,
		FilterOptions filterOptions
	){
		if(filterOptions.getDatabaseId() != null){
			if(!filterOptions.getDatabaseId().equals(event.getDatabase())){
				return false;
			}
		}

		if(filterOptions.getObjectIds() != null && !filterOptions.getObjectIds().isEmpty()){
			if(!filterOptions.getObjectIds().contains(event.getObjectId())){
				return false;
			}
		}

		if(filterOptions.getObjectName() != null && !filterOptions.getObjectName().isEmpty()){
			if(!filterOptions.getObjectName().contains(event.getObjectName())){
				return false;
			}
		}

		if(filterOptions.getEventType() != null && !filterOptions.getEventType().isEmpty()){
			if(!filterOptions.getEventType().contains(event.getEventType())){
				return false;
			}
		}

		return true;
	}

	/**
	 * Filter options for the history event filter.
	 * <p>
	 * Fields are optional and are AND'ed together in the filter.
	 * <p>
	 * Values in lists are OR'ed together.
	 */
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class FilterOptions {
		private String databaseId;

		private Collection<String> objectIds;
		private Collection<String> objectName;
		private Collection<String> eventType;
	}

}
