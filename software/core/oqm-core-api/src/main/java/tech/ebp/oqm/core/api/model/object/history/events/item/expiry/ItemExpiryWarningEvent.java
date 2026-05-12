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
@Schema(title = "ItemExpiryWarningEvent", description = "An event describing when an item expiry warning happens.")
public class ItemExpiryWarningEvent extends ItemExpiryEvent {
	
	public ItemExpiryWarningEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemExpiryWarningEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	@Override
	@Schema(constValue = "ITEM_EXPIRY_WARNING", readOnly = true, required = true, examples = "ITEM_EXPIRY_WARNING")
	public EventType getType() {
		return EventType.ITEM_EXPIRY_WARNING;
	}

}
