package tech.ebp.oqm.baseStation.model.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.EventType;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
@BsonDiscriminator
public class ItemLowStockEvent extends ObjectHistoryEvent {
	
	public ItemLowStockEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemLowStockEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	private ObjectId storageBlockId = null;
	
	private String identifier = null;
	
	private Integer index = null;
	
	@Override
	public EventType getType() {
		return EventType.ITEM_LOW_STOCK;
	}
}
