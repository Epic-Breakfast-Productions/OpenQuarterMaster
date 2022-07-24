package com.ebp.openQuarterMaster.lib.core.history;

import com.ebp.openQuarterMaster.lib.core.storage.items.stored.Stored;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

/**
 * Describes an event in an object's history.
 * <p>
 * TODO:: validator to ensure type
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AddRemoveItemHistoryEvent extends HistoryEvent {
	
	{
		this.setType(EventType.ITEM_ADD_REM);
	}
	
	public AddRemoveItemHistoryEvent() {
		super(EventType.ITEM_ADD_REM);
	}
	
	/**
	 * The id of the storage block that was added/removed from
	 */
	private ObjectId storageId;
	/**
	 * The data about what was added/removed.
	 */
	private Stored stored;
	
	public AddRemoveItemHistoryEvent(
		@NonNull @NotNull EventAction action,
		ObjectId userId,
		ObjectId storageId,
		Stored stored
	) {
		super(action, userId);
		this.setStorageId(storageId);
		this.setStored(stored);
	}
	
	public AddRemoveItemHistoryEvent(
		@NonNull @NotNull EventAction action,
		ObjectId userId,
		@NonNull @NotNull ZonedDateTime timestamp,
		ObjectId storageId,
		Stored stored
	) {
		super(action, userId, timestamp);
		this.setStorageId(storageId);
		this.setStored(stored);
	}
	
	public AddRemoveItemHistoryEvent(
		@NonNull @NotNull EventAction action,
		ObjectId userId,
		@NonNull @NotNull ZonedDateTime timestamp,
		@NonNull @NotNull String description,
		ObjectId storageId,
		Stored stored
	) {
		super(action, userId, timestamp, description);
		this.setStorageId(storageId);
		this.setStored(stored);
	}
}
