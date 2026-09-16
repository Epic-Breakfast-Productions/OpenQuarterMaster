package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.DescriptiveEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;

import javax.measure.Quantity;


@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
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
