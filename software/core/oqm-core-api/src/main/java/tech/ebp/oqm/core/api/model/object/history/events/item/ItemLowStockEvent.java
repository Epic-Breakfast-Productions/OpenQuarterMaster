package tech.ebp.oqm.core.api.model.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator
@SuperBuilder(toBuilder = true)
@Schema(title = "ItemLowStockEvent", description = "An event describing when an item goes into a low stock state.")
public class ItemLowStockEvent extends ItemExpiryLowStockEvent {
	
	public ItemLowStockEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemLowStockEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}

	@NonNull
	private ObjectId transactionId;

	private ObjectId storedId;

	private ObjectId storageBlockId;

	@Override
	@Schema(constValue = "ITEM_LOW_STOCK", readOnly = true, required = true, examples = "ITEM_LOW_STOCK")
	public EventType getType() {
		return EventType.ITEM_LOW_STOCK;
	}

}
