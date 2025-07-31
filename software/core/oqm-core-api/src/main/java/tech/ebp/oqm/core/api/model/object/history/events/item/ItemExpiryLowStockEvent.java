package tech.ebp.oqm.core.api.model.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator
@SuperBuilder(toBuilder = true)
public abstract class ItemExpiryLowStockEvent extends ObjectHistoryEvent {
	public ItemExpiryLowStockEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}

	public ItemExpiryLowStockEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
}
