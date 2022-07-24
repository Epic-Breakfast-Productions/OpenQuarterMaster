package com.ebp.openQuarterMaster.lib.core.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

/**
 * Describes an event in an object's history.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class HistoryEvent {
	/**
	 * The type of event that occurred
	 */
	@NonNull
	@NotNull
	private EventType type;
	
	/**
	 * The user that performed the event Not required to be anything, as in some niche cases there wouldn't be one (adding user)
	 */
	private ObjectId userId;
	
	/**
	 * When the event occurred
	 */
	@lombok.Builder.Default
	@NonNull
	@NotNull
	private ZonedDateTime timestamp = ZonedDateTime.now();
	
	/** Description of the event */
	@lombok.Builder.Default
	@NonNull
	@NotNull
	private String description = "";
}
