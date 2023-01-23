package tech.ebp.oqm.lib.core.object.history.events.item.expiry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemStorageBlockEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
public abstract class ItemExpiryEvent extends ItemStorageBlockEvent {
	
	public ItemExpiryEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemExpiryEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
}
