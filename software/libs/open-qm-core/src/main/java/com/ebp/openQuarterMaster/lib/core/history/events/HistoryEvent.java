package com.ebp.openQuarterMaster.lib.core.history.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
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
	@JsonSubTypes.Type(value = CreateEvent.class, name = "CREATE"),
	@JsonSubTypes.Type(value = UpdateEvent.class, name = "UPDATE"),
	@JsonSubTypes.Type(value = DeleteEvent.class, name = "DELETE"),
	@JsonSubTypes.Type(value = UserLoginEvent.class, name = "USER_LOGIN"),
	@JsonSubTypes.Type(value = ItemAddEvent.class, name = "ITEM_ADD"),
	@JsonSubTypes.Type(value = ItemSubEvent.class, name = "ITEM_SUBTRACT"),
	@JsonSubTypes.Type(value = ItemTransferEvent.class, name = "ITEM_TRANSFER")
})
@SuperBuilder
@BsonDiscriminator
public abstract class HistoryEvent {
	
	/**
	 * The type of event. This field exists to safisfy Jackson, as lombok hates fields in json and not have an actual field. As Lombok can
	 * override this field in the builder, we have {@link #getType()} as an abstract method to force any retrieval of the type to be what
	 * the class should show.
	 */
	@Setter(AccessLevel.PROTECTED)
	private EventType type;
	
	/**
	 * The user that performed the event Not required to be anything, as in some niche cases there wouldn't be one (adding user)
	 */
	private ObjectId userId;
	
	/**
	 * When the event occurred
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private ZonedDateTime timestamp = ZonedDateTime.now();
	
	protected HistoryEvent(EventType type) {
		timestamp = ZonedDateTime.now();
		this.type = type;
	}
	
	public abstract EventType getType();
	
}
