package tech.ebp.oqm.core.api.model.object.history.events.item.expiry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

/**
 * Event for the addition of items to a storage block.
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator
@SuperBuilder(toBuilder = true)
@Schema(title = "ItemExpiredEvent", description = "An event describing when a stored item becomes expired.")
public class ItemExpiredEvent extends ItemExpiryEvent {
	
	public ItemExpiredEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemExpiredEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	@Override
	@Schema(constValue = "ITEM_EXPIRED", readOnly = true, required = true, examples = "ITEM_EXPIRED")
	public EventType getType() {
		return EventType.ITEM_EXPIRED;
	}
}
