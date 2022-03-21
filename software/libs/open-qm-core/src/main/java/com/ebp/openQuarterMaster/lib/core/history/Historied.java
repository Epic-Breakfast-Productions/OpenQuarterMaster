package com.ebp.openQuarterMaster.lib.core.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes an object that has a history.
 */
@Data
@NoArgsConstructor
public abstract class Historied {
	
	/**
	 * The list of history events. Modified by the base station only.
	 * <p>
	 * Don't directly modify the values in this list, use {@link #updated(HistoryEvent)} to add a new event.
	 */
	@NonNull
	@NotNull
	private List<@NotNull HistoryEvent> history = new ArrayList<>();
	
	/**
	 * Adds a history event to the set held, to the front of the list.
	 *
	 * @param event The event to add
	 *
	 * @return This historied object.
	 */
	@JsonIgnore
	public Historied updated(@NonNull HistoryEvent event) {
		if (this.history.isEmpty() && !EventType.CREATE.equals(event.getType())) {
			throw new IllegalArgumentException("First event must be CREATE");
		}
		if (!this.history.isEmpty() && EventType.CREATE.equals(event.getType())) {
			throw new IllegalArgumentException("Cannot add another CREATE event type.");
		}
		
		this.getHistory().add(0, event);
		return this;
	}
	
	/**
	 * Gets the last event that occurred in this object's history.
	 *
	 * @return The last event in the object's history
	 */
	@BsonIgnore
	@JsonIgnore
	public HistoryEvent lastHistoryEvent() {
		return this.getHistory().get(0);
	}
	
	/**
	 * Gets the time of the last events in the object's history.
	 * <p>
	 * Wrapper for {@link #lastHistoryEvent()}
	 *
	 * @return the time of the last events in the object's history.
	 */
	@BsonIgnore
	@JsonIgnore
	public ZonedDateTime lastHistoryEventTime() {
		return this.lastHistoryEvent().getTimestamp();
	}
}
