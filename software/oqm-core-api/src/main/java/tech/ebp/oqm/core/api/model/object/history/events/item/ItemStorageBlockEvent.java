package tech.ebp.oqm.core.api.model.object.history.events.item;

import jakarta.validation.constraints.NotNull;
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

	@lombok.Builder.Default
	private String identifier = null;

	@lombok.Builder.Default
	private Integer index = null;
}
