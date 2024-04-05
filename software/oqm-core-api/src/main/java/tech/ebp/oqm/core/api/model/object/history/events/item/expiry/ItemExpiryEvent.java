package tech.ebp.oqm.core.api.model.object.history.events.item.expiry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemStorageBlockEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
@BsonDiscriminator
public abstract class ItemExpiryEvent extends ItemStorageBlockEvent {
	
	public ItemExpiryEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemExpiryEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
}
