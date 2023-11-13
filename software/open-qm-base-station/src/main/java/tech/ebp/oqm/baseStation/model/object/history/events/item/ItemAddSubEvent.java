package tech.ebp.oqm.baseStation.model.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.DescriptiveEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

import javax.measure.Quantity;


@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
@BsonDiscriminator
public abstract class ItemAddSubEvent
	extends DescriptiveEvent
{
	
	public ItemAddSubEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemAddSubEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	
	private Quantity<?> quantity;
}
