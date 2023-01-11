package tech.ebp.oqm.lib.core.object.history;

import lombok.ToString;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.events.CreateEvent;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes an object's history.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ObjectHistoryEvent extends MainObject {
	
	/**
	 * The id of the object this history is for
	 */
	private ObjectId objectId;
	
	/**
	 * The list of history events. Modified by the base station only.
	 * <p>
	 * Don't directly modify the values in this list, use {@link #updated(HistoryEvent)} to add a new event.
	 */
	@NonNull
	@NotNull
	private List<@NotNull HistoryEvent> history = new ArrayList<>();
	
	public ObjectHistoryEvent(ObjectId objectId, InteractingEntity entity) {
		this.objectId = objectId;
		this.updated(
			CreateEvent.builder()
				.entityId((entity == null ? null : entity.getId()))
				.entityType((entity == null ? null : entity.getInteractingEntityType()))
				.build()
		);
	}
	
	public ObjectHistoryEvent(MainObject object, InteractingEntity entity) {
		this(object.getId(), entity);
	}
	
	/**
	 * Adds a history event to the set held, to the front of the list.
	 *
	 * @param event The event to add
	 *
	 * @return This historied object.
	 */
	@JsonIgnore
	public ObjectHistoryEvent updated(@NonNull HistoryEvent event) {
		if (this.history.isEmpty() && !(event instanceof CreateEvent)) {
			throw new IllegalArgumentException("First event must be a create");
		}
		if (!this.history.isEmpty() && (event instanceof CreateEvent)) {
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
