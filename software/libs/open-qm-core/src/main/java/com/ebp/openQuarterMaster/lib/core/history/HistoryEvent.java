package com.ebp.openQuarterMaster.lib.core.history;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

/**
 * Describes an event in an object's history.
 * <p>
 * TODO:: validator to ensure type
 */
@Data
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = HistoryEvent.class, name = "CRUD"),
	@JsonSubTypes.Type(value = AddRemoveItemHistoryEvent.class, name = "ITEM_ADD_REM")
})
public class HistoryEvent {
	
	/**
	 * The type of event. Used for jackson.
	 */
	@Setter(AccessLevel.PROTECTED)
	protected EventType type;
	
	/**
	 * The type of event that occurred
	 */
	@NonNull
	@NotNull
	private EventAction action;
	
	/**
	 * The user that performed the event Not required to be anything, as in some niche cases there wouldn't be one (adding user)
	 */
	private ObjectId userId;
	
	/**
	 * When the event occurred
	 */
	@NonNull
	@NotNull
	private ZonedDateTime timestamp = ZonedDateTime.now();
	
	/** Description of the event */
	@NonNull
	@NotNull
	private String description = "";
	
	{
		this.setType(EventType.CRUD);
	}
	
	protected HistoryEvent(EventType type) {
		this.type = type;
	}
	
	public HistoryEvent() {
		this(EventType.CRUD);
	}
	
	public HistoryEvent(
		@NonNull @NotNull EventAction action,
		ObjectId userId
	) {
		this();
		this.action = action;
		this.userId = userId;
	}
	
	public HistoryEvent(
		@NonNull @NotNull EventAction action,
		ObjectId userId,
		@NonNull @NotNull ZonedDateTime timestamp
	) {
		this();
		this.action = action;
		this.userId = userId;
		this.timestamp = timestamp;
	}
	
	public HistoryEvent(
		@NonNull @NotNull EventAction action,
		ObjectId userId,
		@NonNull @NotNull ZonedDateTime timestamp,
		@NonNull @NotNull String description
	) {
		this();
		this.action = action;
		this.userId = userId;
		this.timestamp = timestamp;
		this.description = description;
	}
}
