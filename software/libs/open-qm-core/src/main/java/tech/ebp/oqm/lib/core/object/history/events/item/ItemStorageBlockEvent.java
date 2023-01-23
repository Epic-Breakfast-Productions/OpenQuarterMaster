package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
public abstract class ItemStorageBlockEvent extends ObjectHistoryEvent {
	
	public ItemStorageBlockEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemStorageBlockEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	@NotNull
	//	@NonNull
	private ObjectId storageBlockId;
	
	private String identifier = null;
	
	private Integer index = null;
}
