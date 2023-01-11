package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.object.history.EventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

/**
 * Event for the subtraction of items from a storage block.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class ItemSubEvent extends ItemAddSubEvent {
	
	@NonNull
	@NotNull
	private ObjectId storageBlockId;
	
	@Override
	public EventType getType() {
		return EventType.ITEM_SUBTRACT;
	}
}
